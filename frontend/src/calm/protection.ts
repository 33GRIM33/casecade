import { Capacitor,registerPlugin } from '@capacitor/core';
import { focus } from '../focus';
import { routeNotification } from '../notificationPolicy';
export type Protection={policy:'focus_first'|'balanced';scope:'focus'|'always';criteria:string;holdMinutes:number;blockPromotions?:boolean;active?:boolean};
export type Guard={enabled:boolean;intervalSeconds:number;dailyBudgetMinutes:number;instagramLimitMinutes:number;youtubeLimitMinutes:number;limitMode:'remind'|'block';extensionUntil:number;blockingVisible:boolean;blockError:string;instagram:boolean;youtube:boolean;shortsOnly:boolean;overlay:boolean;pausedUntil:number;accessGranted:boolean;connected:boolean;activeApp:string;screenStatus:string;continuousSeconds:number;nextInSeconds:number;instagramSeconds:number;youtubeSeconds:number;scrollEvents:number;remindersToday:number;overlayVisible:boolean;consent?:boolean};
export const protectionDefaults:Protection={policy:'focus_first',scope:'focus',criteria:'',holdMinutes:15,blockPromotions:true};
export const guardDefaults:Guard={enabled:false,intervalSeconds:300,dailyBudgetMinutes:0,instagramLimitMinutes:0,youtubeLimitMinutes:0,limitMode:'remind',extensionUntil:0,blockingVisible:false,blockError:'',instagram:true,youtube:true,shortsOnly:false,overlay:true,pausedUntil:0,accessGranted:false,connected:false,activeApp:'',screenStatus:'Android accessibility required',continuousSeconds:0,nextInSeconds:300,instagramSeconds:0,youtubeSeconds:0,scrollEvents:0,remindersToday:0,overlayVisible:false};
type Api={getProtection():Promise<Protection>;setProtection(p:Protection):Promise<Protection>;setAllRoles(o:{role:string;keepImportant:boolean;throughoutDay:boolean}):Promise<unknown>;previewNotification(p:{pkg:string;title:string;text:string}):Promise<{action:string;reason:string;active:boolean}>;getScrollGuard():Promise<Guard>;setScrollGuard(p:Guard):Promise<Guard>;openScrollAccess():Promise<void>;testScrollGuard():Promise<void>;testScrollBlock():Promise<void>};
const plugin=registerPlugin<Api>('Focus'),native=Capacitor.isNativePlatform();
const read=<T,>(key:string,defaults:T):T=>{try{return {...defaults,...JSON.parse(localStorage.getItem(key)||'{}')};}catch{return defaults;}};
export const protection={
 async get(){if(native)return plugin.getProtection();const p=read('calm.protection',protectionDefaults),state=await focus.getState();return {...p,active:!state.paused&&(p.scope==='always'||state.focusActive)};},
 async save(p:Protection){if(native)return plugin.setProtection(p);localStorage.setItem('calm.protection',JSON.stringify(p));return this.get();},
 async all(role:'SMART'|'BLOCK_IN_FOCUS'|'ALLOW',keepImportant:boolean,throughoutDay:boolean){if(native)return plugin.setAllRoles({role,keepImportant,throughoutDay});const apps=await focus.getApps();for(const app of apps.apps)if(!(keepImportant&&app.role==='IMPORTANT'))await focus.setRole(app.packageName,role);localStorage.setItem('calm.defaultRole',role);if(role==='ALLOW')await this.save({...await this.get(),blockPromotions:false});if(throughoutDay){await this.save({...await this.get(),scope:'always'});await focus.setPaused(false);}},
 async preview(pkg:string,text:string){if(native)return plugin.previewNotification({pkg,title:'Sample',text});const apps=await focus.getApps(),settings=await focus.getSettings(),p=await this.get();return {...routeNotification({focusActive:true,role:apps.apps.find(a=>a.packageName===pkg)?.role||'ALLOW',text,urgentWords:settings.urgentWords,vipContacts:settings.vipContacts}),active:!!p.active};},
 async guard(){return native?plugin.getScrollGuard():read('calm.guard',guardDefaults);},
 async saveGuard(g:Guard){if(native)return plugin.setScrollGuard(g);localStorage.setItem('calm.guard',JSON.stringify(g));return g;},
 async openAccess(){if(native)await plugin.openScrollAccess();},
 async testGuard(){if(!native)throw Error('Automatic reminders need the Android APK.');await plugin.testScrollGuard();},
 async testBlock(){if(!native)throw Error('Pause screens need the Android APK and Accessibility service.');await plugin.testScrollBlock();},
 clearPreview(){for(const key of ['calm.guard','calm.protection','calm.defaultRole','calm.break.activity','calm.inbox.v5'])localStorage.removeItem(key);}
};
