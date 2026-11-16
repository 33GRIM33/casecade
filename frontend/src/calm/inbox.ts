import { Capacitor,registerPlugin } from '@capacitor/core';
export type InboxMessage={id:number;group_key:string;pkg:string;label:string;title:string;text:string;state:string;reason:string;ts:number;source:string;read:boolean};
export type InboxSummary={group_key:string;text:string;reviewed:number;updated:number};
export type Inbox={messages:InboxMessage[];summaries:InboxSummary[];revision?:number;unchanged?:boolean};
const plugin=registerPlugin<{getInbox(o:{tests:boolean;since:number}):Promise<Inbox>;markInboxRead(o:{group:string}):Promise<void>;retryInbox():Promise<void>;clearInbox():Promise<void>}>('Focus');
const cache:Partial<Record<'real'|'test',Inbox>>={};
const key='calm.inbox.v5';
export const inbox={
 async get(tests=false):Promise<Inbox>{if(Capacitor.isNativePlatform()){const source=tests?'test':'real',old=cache[source],next=await plugin.getInbox({tests,since:old?.revision??-1});if(next.unchanged&&old)return old;cache[source]=next;return next;}let messages:InboxMessage[]=[];try{messages=JSON.parse(localStorage.getItem(key)||'[]');}catch{}return {messages:messages.filter(m=>m.source===(tests?'test':'real')),summaries:[]};},
 async read(group:string){if(Capacitor.isNativePlatform())return plugin.markInboxRead({group});const all=JSON.parse(localStorage.getItem(key)||'[]') as InboxMessage[];localStorage.setItem(key,JSON.stringify(all.map(m=>!group||m.group_key===group?{...m,read:true}:m)));},
 async retry(){if(Capacitor.isNativePlatform())await plugin.retryInbox();},
 async clear(){if(Capacitor.isNativePlatform())await plugin.clearInbox();delete cache.real;delete cache.test;localStorage.removeItem(key);},
 sample(message:Omit<InboxMessage,'id'|'read'|'group_key'>){const all=JSON.parse(localStorage.getItem(key)||'[]') as InboxMessage[];all.unshift({...message,id:Date.now(),read:false,group_key:message.pkg+'|'+message.title});localStorage.setItem(key,JSON.stringify(all.slice(0,500)));}
};
