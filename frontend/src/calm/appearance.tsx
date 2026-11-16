import { useEffect,useState } from 'react';
import { Capacitor,registerPlugin } from '@capacitor/core';
import type { Page,CalmStore } from './store';
type Theme='system'|'light'|'dark';
const plugin=registerPlugin<{getAppearance():Promise<{theme:Theme;route?:string}>;setAppearance(o:{theme:Theme}):Promise<void>;pinWidget():Promise<void>}>('Focus');
const read=():Theme=>{const t=localStorage.getItem('calm.theme');return t==='light'||t==='dark'?t:'system';};
export function useAppearance(go:(page:Page)=>void){const [theme,setTheme]=useState<Theme>(read),[systemDark,setSystemDark]=useState(()=>matchMedia('(prefers-color-scheme: dark)').matches);const dark=theme==='dark'||theme==='system'&&systemDark;
 useEffect(()=>{const media=matchMedia('(prefers-color-scheme: dark)'),update=()=>setSystemDark(media.matches);media.addEventListener('change',update);return()=>media.removeEventListener('change',update);},[]);
 useEffect(()=>{document.documentElement.dataset.theme=dark?'dark':'light';localStorage.setItem('calm.theme',theme);},[dark,theme]);
 useEffect(()=>{if(!Capacitor.isNativePlatform())return;const load=()=>void plugin.getAppearance().then(p=>{setTheme(p.theme);if(p.route&&['home','focus','scroll','alerts','reflect','settings','bedtime','messages'].includes(p.route))go(p.route as Page);}).catch(()=>{});load();window.addEventListener('attention-route',load);return()=>window.removeEventListener('attention-route',load);},[]);
 async function choose(next:Theme){if(Capacitor.isNativePlatform())await plugin.setAppearance({theme:next});setTheme(next);}
 return {theme,dark,choose};
}
export type Appearance=ReturnType<typeof useAppearance>;
export function AppearancePanel({s,a}:{s:CalmStore;a:Appearance}){return <section className="surface"><h2>Theme & home widget</h2><label htmlFor="app-theme">App appearance</label><select id="app-theme" value={a.theme} onChange={e=>void s.run(()=>a.choose(e.target.value as Theme))}><option value="system">Follow phone theme</option><option value="light">Light · warm cream</option><option value="dark">Dark · forest night</option></select><div className={`widget-preview ${a.dark?'night':''}`}><b>AttentionTwin</b><span>Your focus and scrolling goals, at a glance.</span><div>Focus　·　Scroll goals　·　Theme</div></div><button className="pill-button outlined full" disabled={!Capacitor.isNativePlatform()||s.busy} onClick={()=>void s.run(async()=>{await plugin.pinWidget();s.toast('Finish adding the widget in your launcher. Its Theme button switches light/dark independently.');})}>Add home-screen widget</button><p className="caption">The Android widget has Focus and Scroll goals shortcuts, local daily usage, and its own light/dark button. You can also add it by long-pressing Home → Widgets → AttentionTwin.</p></section>;}
