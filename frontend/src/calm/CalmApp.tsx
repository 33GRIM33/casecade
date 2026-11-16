import { useKeyboard } from './keyboard';
import { useAppearance } from './appearance';
import { useEffect, useRef, useState } from 'react';
import { ArrowLeft,Bell,BellOff,BookOpen,Heart,Home,Leaf,Settings,Smartphone,X,Moon,Sun } from 'lucide-react';
import { focus } from '../focus';
import { native,useCalmStore } from './store';
import type { Page } from './store';
import { HomePage,FocusPage } from './StudyPages';
import { BedtimePage,ScrollPage,ReflectPage } from './WellnessPages';
import { AlertsPage,SettingsPage } from './AlertPages';
import './calm.css';
export default function CalmApp(){
 const keyboard=useKeyboard();const s=useCalmStore();const appearance=useAppearance(s.go);const [erase,setErase]=useState(false);const scroll=useRef<HTMLElement>(null);
 useEffect(()=>{scroll.current?.scrollTo({top:0});},[s.page]);
 const child=['bedtime','scroll','settings'].includes(s.page),main=child?'home':s.page==='messages'?'alerts':s.page;
 const titles:Record<Page,string>={home:'Your daily pause',focus:'Focus & breaks',alerts:'Notifications',reflect:'Your reflections',bedtime:'Evening ritual',scroll:'Mindful scrolling',settings:'Your preferences',messages:'Important inbox'};
 return<div className="calm-app"><header className="calm-header"><button className="brand-button" aria-label={child?'Back to home':'AttentionTwin home'} onClick={()=>s.go('home')}>{child?<ArrowLeft/>:<Leaf/>}</button><div><span className="brand-title">AttentionTwin</span><span className="brand-subtitle">{titles[s.page]}</span></div><button className="brand-button theme-toggle" aria-label={appearance.dark?"Switch to light theme":"Switch to dark theme"} onClick={()=>void s.run(()=>appearance.choose(appearance.dark?"light":"dark"))}>{appearance.dark?<Sun size={19}/>:<Moon size={19}/>}</button><button className="text-link keyboard-done" hidden={!keyboard.open} onClick={()=>void keyboard.close()}>Done typing</button><button className="avatar-button" aria-label="Open preferences" onClick={()=>s.go('settings')}>{s.data.name?s.data.name.slice(0,1).toUpperCase():<Settings size={20}/>}</button></header>
  <main className="calm-main" ref={scroll} id="main-content">{!native&&<div className="preview-note"><Smartphone size={14}/> Browser preview · Phone controls work in the APK</div>}{s.permissionWarning&&<div className="inline-notice"><BellOff size={18}/><div>Notifications are off. Your timers still work here.<button className="text-link" onClick={()=>void s.run(()=>focus.openAppSettings())}>Open phone settings</button></div></div>}
   {s.page==='home'&&<HomePage s={s}/>} {s.page==='focus'&&<FocusPage s={s}/>} {(s.page==='alerts'||s.page==='messages')&&<AlertsPage key={s.page} s={s}/>} {s.page==='bedtime'&&<BedtimePage s={s}/>} {s.page==='scroll'&&<ScrollPage s={s}/>} {s.page==='reflect'&&<ReflectPage s={s}/>} {s.page==='settings'&&<SettingsPage s={s} appearance={appearance} onErase={()=>setErase(true)}/>}
   <footer className="calm-footer"><Leaf size={14}/> Small steps. A little more space.</footer>
  </main><nav className="calm-nav" aria-label="Main navigation">{[{page:'home',label:'Home',icon:<Home/>},{page:'focus',label:'Focus',icon:<BookOpen/>},{page:'alerts',label:'Alerts',icon:<><Bell/>{s.inboxUnread>0&&<i className="nav-badge">{s.inboxUnread}</i>}</>},{page:'reflect',label:'Reflect',icon:<Heart/>}].map(t=><button key={t.page} aria-label={t.label} className={main===t.page?'selected':''} aria-current={main===t.page?'page':undefined} onClick={()=>s.go(t.page as Page)}><span>{t.icon}</span><small>{t.label}</small></button>)}</nav>
  {s.notice&&<div className="toast-message" role="status"><span>{s.notice}</span><button aria-label="Dismiss message" onClick={()=>s.setNotice('')}><X size={16}/></button></div>}{s.busy&&<span className="busy-indicator" role="status">Saving your changes…</span>}
  {erase&&<div className="modal-backdrop"><section className="confirm-dialog" role="alertdialog" aria-modal="true" aria-labelledby="erase-title" onKeyDown={e=>{if(e.key==="Escape")setErase(false);if(e.key==="Tab"){const buttons=Array.from(e.currentTarget.querySelectorAll<HTMLButtonElement>("button:not(:disabled)"));const first=buttons[0],last=buttons[buttons.length-1];if(e.shiftKey&&document.activeElement===first){e.preventDefault();last?.focus();}else if(!e.shiftKey&&document.activeElement===last){e.preventDefault();first?.focus();}}}}><h2 id="erase-title">A fresh start?</h2><p>This erases your planner, reflections, notification history, app choices, Gemini key and reminders from this device.</p><button className="danger-button full" disabled={s.busy} onClick={()=>void s.run(async()=>{await s.erase();setErase(false);})}>Erase all local data</button><button className="pill-button outlined full" autoFocus onClick={()=>setErase(false)}>Keep my data</button></section></div>}
 </div>;
}

