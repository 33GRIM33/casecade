export type Session = { id: number; task: string; minutes: number; breakMinutes: number; end: number; remaining: number; phase: 'focus' | 'break'; paused: boolean; planId?: number };
export type Reflection = { day: string; mood: string; note: string };
export type Completed = { id: number; task: string; minutes: number; at: number };
export type PlanTask = { id: number; title: string; minutes: number; done: boolean; day: string };
export type WellnessData = { task: string; minutes: number; breakMinutes: number; session: Session | null; completed: Completed[]; reflections: Reflection[]; bedtime: string; windDown: number; bedtimeEnabled: boolean; checks: string[]; checkDay: string; scrollEnd: number; scrollMinutes: number; scrollCheckins: number[]; tasks: PlanTask[]; bedtimeAt: number; bedtimeDaily: boolean; name: string };
export const STORAGE_KEY = 'attentiontwin.wellness.v1';
export const localDay = (time = Date.now()) => { const d = new Date(time); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`; };
export const initialWellness = (): WellnessData => ({ task: '', minutes: 25, breakMinutes: 5, session: null, completed: [], reflections: [], bedtime: '22:30', windDown: 30, bedtimeEnabled: false, checks: [], checkDay: localDay(), scrollEnd: 0, scrollMinutes: 10, scrollCheckins: [], tasks: [], bedtimeAt: 0, bedtimeDaily: true, name: '' });
export function secondsLeft(session: Session, now = Date.now()) { return session.paused ? session.remaining : Math.min(session.remaining, Math.max(0, Math.ceil((session.end - now) / 1000))); }
export function nextWindDown(bedtime: string, minutes: number, now = Date.now()) { const [h,m] = bedtime.split(':').map(Number); const d = new Date(now); d.setHours(h,m,0,0); d.setMinutes(d.getMinutes()-minutes); if(d.getTime() <= now) d.setDate(d.getDate()+1); return d.getTime(); }
export function completeFocus(data: WellnessData, now = Date.now()): WellnessData { const s = data.session; if(!s || s.phase !== 'focus' || secondsLeft(s,now)>0 || data.completed.some(x=>x.id===s.id)) return data; return { ...data, completed: [...data.completed, {id:s.id,task:s.task,minutes:s.minutes,at:s.end}].slice(-365), tasks: (data.tasks||[]).map(t=>t.id===s.planId?{...t,done:true}:t) }; }

export function loadWellness(raw: string | null): WellnessData {
 try { const value=JSON.parse(raw||'null'); if(!value || typeof value!=='object') return initialWellness(); const merged={...initialWellness(),...value};
  for(const key of ['tasks','completed','reflections','checks','scrollCheckins'] as const) if(!Array.isArray(merged[key])) (merged as any)[key]=[];
  if(![1,15,25,45,50,90].includes(merged.minutes))merged.minutes=25;
  if(merged.session && (!Number.isFinite(merged.session.end)||!['focus','break'].includes(merged.session.phase)))merged.session=null;
  return merged;
 } catch { return initialWellness(); }
}

