import type { Action,Role } from './focus';
type Input={focusActive:boolean;role:Role;title?:string;text?:string;urgentWords?:string;vipContacts?:string;notificationCategory?:string;blockPromotions?:boolean};
export function localNotificationKind(title='',text='',urgentWords=''){
 const content=`${title} ${text}`.toLowerCase().replace(/’/g,"'").replace(/\s+/g,' ').trim(),normal=text.toLowerCase().replace(/’/g,"'").replace(/^[^:\n]{1,60}:\s*/,'').replace(/[^\p{L}\p{N}\s]/gu,' ').replace(/\s+/g,' ').trim();
 const drill=/\b(fire sale|fire drill|fire training)\b/.test(content)&&!content.includes('not a drill');
 if(!drill&&(/\b(there is|there's|urgent|emergency|house|building|kitchen|room|help).{0,45}\b(fire|gas leak|smoke|accident|bleeding|ambulance|danger)\b/.test(content)||/\b(on fire|cannot breathe|can't breathe|call (112|911|999|ambulance|police)|medical emergency|help me now|i need help now)\b/.test(content)))return 'EMERGENCY';
 if(/\b(flash sale|fire sale|limited[- ]time offer|special offer|exclusive offer|sale ends|shop now|buy now|coupon|promo code|discount|clearance|deal of the day|mega sale|price drop|unsubscribe|spin (and|to) win|claim.{0,20}(reward|cashback|offer)|earn.{0,20}cashback|get.{0,20}cashback)\b/.test(content)||/\b(save|flat|extra|upto|up to)\b.{0,18}\d{1,3}\s*%|\d{1,3}\s*%\s*(off|discount)|buy\s+\d+.{0,20}get\s+\d+/.test(content))return 'PROMOTION';
 if(/\b(otp|one[- ]time (password|code)|verification code|security code|passcode)\b/.test(content))return 'IMPORTANT';
 if(urgentWords.split(',').some(w=>w.trim().length>=2&&new RegExp(`(?<![\\p{L}\\p{N}])${w.trim().toLowerCase().replace(/[.*+?^${}()|[\]\\]/g,'\\$&')}(?![\\p{L}\\p{N}])`,'u').test(content))||/\b(deadline|appointment|exam change|meeting moved|delivery needs|important)\b/.test(content))return 'IMPORTANT';
 if(/^((hello+|helo+|heelo+|hi+|hey+|hlo|yo)\s*){1,6}(bro|bhai|there|guys|all|yaar|[0-9 ]+)?$/.test(normal)||/^(namaste|good morning|good evening|good night|how a+re (you|u)|how r u|what s up|whats up|kya haal hai|kaise ho)$/.test(normal)||/^(hey )?(let s|lets|let us) play( some)? (games?|coc|pubg|bgmi)$/.test(normal)||/\b(new video|recommended for you|trending now|liked your post|new follower)\b/.test(content))return 'CASUAL';
 return 'PENDING';
}
export function routeNotification(input:Input):{action:Action;reason:string;exception:boolean;state?:string}{
 if(!input.focusActive)return{action:'ALLOW',reason:'Filtering is off; alert left available.',exception:false};
 const content=`${input.title||''} ${input.text||''}`.toLowerCase(),kind=localNotificationKind(input.title,input.text,input.urgentWords),sender=(input.text||'').match(/^([^:\n]{1,80}):/)?.[1]||'',identity=`${input.title||''} ${sender}`.toLowerCase(),vip=(input.vipContacts||'').split(',').some(w=>w.trim().length>=2&&new RegExp(`(?<![\\p{L}\\p{N}])${w.trim().toLowerCase().replace(/[.*+?^${}()|[\]\\]/g,'\\$&')}(?![\\p{L}\\p{N}])`,'u').test(identity));
 const code=/\b(otp|one[- ]time (password|code)|verification code|security code|passcode)\b/.test(content);
 if(vip||kind==='EMERGENCY'||/^(call|alarm|navigation|emergency|error)$/.test(input.notificationCategory||'')||(kind!=='PROMOTION'&&code))return{action:'ALLOW',state:'IMPORTANT',reason:'Local safety exception: emergency, VIP, critical alert or verification code.',exception:true};
 if(kind==='PROMOTION'&&input.blockPromotions!==false)return{action:'DEFER',state:'PROMOTION',reason:'Local promotion rule: saved quietly; no Gemini request.',exception:false};
 if(input.role==='IMPORTANT'||input.role==='ALLOW')return{action:'ALLOW',reason:'App rule allows this alert.',exception:false};
 return{action:'DEFER',state:kind==='PENDING'?'UNKNOWN':kind,reason:kind==='CASUAL'?'Local casual rule: saved quietly; no Gemini request.':kind==='IMPORTANT'?'Local importance rule: saved in Important inbox.':'Captured first. Saved for batch or manual review.',exception:false};
}
