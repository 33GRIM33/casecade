import type { ReactNode } from 'react';
export function Title({eyebrow,title,description}:{eyebrow:string;title:string;description:string}){return<section className="page-heading"><span className="eyebrow">{eyebrow}</span><h1>{title}</h1><p>{description}</p></section>;}
export function Empty({icon,title,text}:{icon:ReactNode;title:string;text:string}){return<div className="empty-state">{icon}<h3>{title}</h3><p>{text}</p></div>;}
export function Switch({title,description,checked,onChange}:{title:string;description:string;checked:boolean;onChange:(value:boolean)=>void}){return<label className="switch-row"><span><b>{title}</b><small>{description}</small></span><input type="checkbox" role="switch" checked={checked} onChange={e=>onChange(e.target.checked)}/></label>;}
