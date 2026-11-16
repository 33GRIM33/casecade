import { Capacitor, registerPlugin } from '@capacitor/core';
export type AiConfig={configured:boolean;enabled:boolean;model:string;status:string};
type AiPlugin={getAiConfig():Promise<AiConfig>;configureAi(o:{key:string;enabled:boolean}):Promise<AiConfig>;setAiEnabled(o:{enabled:boolean}):Promise<AiConfig>;clearAi():Promise<void>};
const plugin=registerPlugin<AiPlugin>('Focus');
const empty:AiConfig={configured:false,enabled:false,model:'',status:'Android only'};
export const ai={
 async getConfig(){return Capacitor.isNativePlatform()?plugin.getAiConfig():empty;},
 async configure(key:string,enabled:boolean){if(!Capacitor.isNativePlatform())throw Error('Connect Gemini from the Android app.');return plugin.configureAi({key,enabled});},
 async setEnabled(enabled:boolean){return Capacitor.isNativePlatform()?plugin.setAiEnabled({enabled}):empty;},
 async clear(){if(Capacitor.isNativePlatform())await plugin.clearAi();}
};
