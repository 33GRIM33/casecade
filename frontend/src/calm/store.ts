import { inbox,type Inbox } from './inbox';
import { protection } from './protection';
import { useCallback, useEffect, useRef, useState } from 'react';
import { Capacitor } from '@capacitor/core';
import { focus } from '../focus';
import type { AppEntry, FocusEvent, Role, Settings } from '../focus';
import { completeFocus, initialWellness, loadWellness, localDay, nextWindDown, secondsLeft, STORAGE_KEY } from '../wellnessModel';
import type { Session } from '../wellnessModel';
import { ai, type AiConfig } from './ai';
export type Page='home'|'focus'|'alerts'|'reflect'|'bedtime'|'scroll'|'settings'|'messages';
export const native=Capacitor.isNativePlatform();
export const time=(at:number)=>new Date(at).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'});
export const clock=(seconds:number)=>`${String(Math.floor(seconds/60)).padStart(2,'0')}:${String(seconds%60).padStart(2,'0')}`;
export const moods=[{label:'Tired',face:'☁'},{label:'Distracted',face:'◌'},{label:'Okay',face:'◡'},{label:'Calm',face:'☺'},{label:'Energised',face:'☀'}];
export const rituals=['Write down tomorrow’s first task','Put your phone out of reach','Dim the lights and stretch','Take five slow breaths'];
const defaults:Settings={breakTimes:'',focusSchedule:'',keepPreviews:false,urgentWords:'urgent,emergency,doctor,school',vipContacts:'',silenceAlerts:false};

export function useCalmStore(){
 const [data,setData]=useState(()=>loadWellness(localStorage.getItem(STORAGE_KEY)));
 const [now,setNow]=useState(Date.now()),[page,setPage]=useState<Page>('home');
 const [inboxData,setInboxData]=useState<Inbox>({messages:[],summaries:[]});
 const [phone,setPhone]=useState<any>({}),[apps,setApps]=useState<AppEntry[]>([]),[events,setEvents]=useState<FocusEvent[]>([]);
 const [settings,setSettings]=useState<Settings>(defaults),[ready,setReady]=useState(false),[busy,setBusy]=useState(false),[notice,setNotice]=useState('');
 const [permissionWarning,setPermissionWarning]=useState(false),[mood,setMood]=useState('Okay'),[note,setNote]=useState(''),[planId,setPlanId]=useState<number>();
 const [aiConfig,setAiConfig]=useState<AiConfig>({enabled:false,configured:false,model:'',status:'Not connected'});
 const lock=useRef(false),noticeId=useRef<ReturnType<typeof setTimeout>>();
 useEffect(()=>{localStorage.setItem(STORAGE_KEY,JSON.stringify(data));},[data]);
 useEffect(()=>{const id=setInterval(()=>setNow(Date.now()),1000);const resume=()=>setNow(Date.now());document.addEventListener('visibilitychange',resume);return()=>{clearInterval(id);document.removeEventListener('visibilitychange',resume);};},[]);
 useEffect(()=>{setData(d=>{let next=completeFocus(d,now);if(d.checkDay!==localDay(now))next={...next,checks:[],checkDay:localDay(now)};return next;});},[now]);
 const refresh=useCallback(async()=>{const [s,e,a,i]=await Promise.all([focus.getState(),focus.getEvents('',false),ai.getConfig(),inbox.get()]);setPhone(s);setEvents(e.events);setAiConfig(a);setInboxData(i);},[]);
 useEffect(()=>{let live=true;Promise.all([refresh(),focus.getSettings(),ai.getConfig(),focus.getApps()]).then(([,s,a,list])=>{if(live){setSettings(s);setAiConfig(a);setApps(list.apps);setReady(true);}}).catch(()=>{if(live){setReady(true);setNotice('Phone controls could not load. Your planner is still available.');}});return()=>{live=false;};},[refresh]);
 // Device refreshes never overwrite settings drafts or timer/journal state.
 useEffect(()=>{const id=setInterval(()=>void refresh().catch(()=>{}),3500);const resume=()=>{if(!document.hidden)void refresh().catch(()=>{});};document.addEventListener('visibilitychange',resume);return()=>{clearInterval(id);document.removeEventListener('visibilitychange',resume);};},[refresh]);
 useEffect(()=>()=>{if(noticeId.current)clearTimeout(noticeId.current);},[]);
 function go(p:Page){setPage(p);setNotice('');}
 function toast(text:string){setNotice(text);if(noticeId.current)clearTimeout(noticeId.current);noticeId.current=setTimeout(()=>setNotice(''),6500);}
 async function run(task:()=>Promise<unknown>){if(lock.current)return;lock.current=true;setBusy(true);try{await task();}catch(e){toast(e instanceof Error?e.message:'That did not work. Please try again.');}finally{lock.current=false;setBusy(false);}}
 async function permission(force=false){const result=await focus.requestPostPermission(force);setPermissionWarning(!result.granted);return result.granted;}
 async function reminder(id:number,at:number,title:string,text:string,daily=false){await focus.scheduleReminder(id,at,title,text,daily);}
 async function start(){if(data.session)return;await permission();if((await focus.getState()).paused)await focus.setPaused(false);
  const session:Session={id:Date.now(),task:data.task.trim()||'A little focused study',minutes:data.minutes,breakMinutes:data.breakMinutes,end:Date.now()+data.minutes*60000,remaining:data.minutes*60,phase:'focus',paused:false,planId};
  await focus.setFocus(data.minutes);setData(d=>({...d,session}));
  try{await reminder(901,session.end,'Your focus block is complete','Your planned break is ready. Return when you are ready.');}catch{toast('Session started. Android could not schedule the reminder; the timer still works here.');}void refresh();
 }
 async function toggle(){const s=data.session;if(!s)return;const left=secondsLeft(s);if(!s.paused&&left===0)return;
  if(s.paused){if(s.phase==='focus')await focus.resumeFocus(left);await reminder(901,Date.now()+left*1000,s.phase==='focus'?'Your focus block is complete':'Your break is complete','Return when you feel ready.');}
  else{if(s.phase==='focus')await focus.setFocus(0);await reminder(901,0,'','');}
  setData(d=>({...d,session:{...s,remaining:left,end:Date.now()+left*1000,paused:!s.paused}}));void refresh();
 }
 async function takeBreak(){const s=data.session;if(!s)return;await focus.setFocus(0);const end=Date.now()+s.breakMinutes*60000;await reminder(901,end,'Your break is complete','A fresh start is here whenever you are ready.');setData(d=>({...completeFocus(d),session:{...s,phase:'break',end,remaining:s.breakMinutes*60,paused:false}}));void refresh();}
 async function finish(){await focus.setFocus(0);await reminder(901,0,'','');setData(d=>({...completeFocus(d),session:null}));setPlanId(undefined);void refresh();toast('A little space for yourself. Come back whenever you like.');}
 async function role(pkg:string,value:Role){await focus.setRole(pkg,value);setApps(list=>list.map(a=>a.packageName===pkg?{...a,role:value}:a));toast('Notification preference saved.');}
 async function saveSettings(){await focus.setSettings(settings);toast('Your alert preferences are saved.');}
 async function bedtime(){await permission();const at=nextWindDown(data.bedtime,data.windDown);await reminder(902,at,'Time to wind down','A few small rituals. Let tomorrow wait.',data.bedtimeDaily);setData(d=>({...d,bedtimeEnabled:true,bedtimeAt:at}));toast(`Wind-down reminder set for ${time(at)}${data.bedtimeDaily?' each evening':''}.`);}
 async function cancelBedtime(){await reminder(902,0,'','');setData(d=>({...d,bedtimeEnabled:false,bedtimeAt:0}));toast('Bedtime reminder cancelled.');}
 async function startScroll(){await permission();const at=Date.now()+data.scrollMinutes*60000;await reminder(903,at,'A moment to check in','Is scrolling still what you want to do? Breathe, then choose.');setData(d=>({...d,scrollEnd:at}));toast('A gentle reminder, set.');}
 async function endScroll(save:boolean){await reminder(903,0,'','');setData(d=>({...d,scrollEnd:0,scrollCheckins:save?[...d.scrollCheckins,Date.now()].slice(-365):d.scrollCheckins}));toast(save?'Check-in saved. You choose your next moment.':'Check-in cancelled.');}
 function reflect(){const day=localDay();setData(d=>({...d,reflections:[...d.reflections.filter(r=>r.day!==day),{day,mood,note:note.trim()}].slice(-90)}));setNote('');toast('Today’s reflection is saved.');}
 async function erase(){await focus.clearData();await ai.clear();await inbox.clear();protection.clearPreview();setData(initialWellness());setSettings(defaults);setAiConfig(await ai.getConfig());setApps((await focus.getApps()).apps);setMood('Okay');setNote('');go('home');await refresh();toast('Local data erased. A fresh page.');}
 return{inboxData,inboxUnread:inboxData.messages.filter(m=>!m.read&&(m.state==='IMPORTANT'||m.state==='UNKNOWN')).length,data,setData,now,page,go,phone,apps,setApps,events,settings,setSettings,ready,busy,notice,setNotice,toast,run,permission,permissionWarning,refresh,start,toggle,takeBreak,finish,role,saveSettings,bedtime,cancelBedtime,startScroll,endScroll,mood,setMood,note,setNote,reflect,setPlanId,aiConfig,setAiConfig,erase};
}
export type CalmStore=ReturnType<typeof useCalmStore>;
