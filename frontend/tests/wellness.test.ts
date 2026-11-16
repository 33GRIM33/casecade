import { test } from 'node:test';
import assert from 'node:assert/strict';
import { initialWellness, secondsLeft, completeFocus, nextWindDown, localDay, loadWellness } from '../src/wellnessModel.ts';
test('deadline is authoritative after suspension and never goes negative',()=>{
 const session={id:1,task:'Read',minutes:25,breakMinutes:5,end:100000,remaining:60,paused:false,phase:'focus' as const};
 assert.equal(secondsLeft(session,40000),60); assert.equal(secondsLeft(session,110000),0);
 assert.equal(secondsLeft({...session,paused:true},110000),60);
});
test('a session never shows more than its planned seconds before the next tick',()=>{
 const s={id:1,task:'Read',minutes:1,breakMinutes:1,end:61000,remaining:60,paused:false,phase:'focus' as const};assert.equal(secondsLeft(s,0),60);
});
test('legacy storage migrates safely and preserves reflections',()=>{
 const saved=loadWellness(JSON.stringify({task:'Read',completed:[],reflections:[{day:'2026-11-16',mood:'Okay',note:'Small step'}]}));assert.equal(saved.tasks.length,0);assert.equal(saved.reflections.length,1);assert.equal(saved.bedtimeDaily,true);assert.equal(loadWellness('broken').session,null);
});
test('finishing the full focus block completes its linked plan once',()=>{
 const d=initialWellness();d.tasks=[{id:7,title:'Read',minutes:25,done:false,day:'2026-11-16'}];d.session={id:3,task:'Read',minutes:25,breakMinutes:5,end:1000,remaining:1500,paused:false,phase:'focus',planId:7};const done=completeFocus(d,1001);assert.equal(done.tasks[0].done,true);assert.equal(completeFocus(done,1002),done);
});
test('completion is awarded once and only after the full focus block',()=>{
 const d=initialWellness();d.session={id:1,task:'Read',minutes:25,breakMinutes:5,end:100000,remaining:1500,paused:false,phase:'focus'};
 assert.equal(completeFocus(d,99999).completed.length,0);
 const done=completeFocus(d,100000);assert.equal(done.completed[0].minutes,25);assert.equal(completeFocus(done,120000).completed.length,1);
 assert.equal(completeFocus({...d,session:{...d.session,paused:true}},120000).completed.length,0);
 assert.equal(completeFocus({...d,session:{...d.session,phase:'break'}},120000).completed.length,0);
});
test('wind down correctly crosses midnight and schedules the next future occurrence',()=>{
 const now=new Date(2026,10,16,22,0).getTime(); const at=nextWindDown('00:15',30,now);
 assert.equal(new Date(at).getHours(),23);assert.equal(new Date(at).getMinutes(),45);assert.equal(localDay(at),'2026-11-16');
 const tomorrow=nextWindDown('22:30',30,now);assert.equal(localDay(tomorrow),'2026-11-17');assert.ok(tomorrow>now);
});
