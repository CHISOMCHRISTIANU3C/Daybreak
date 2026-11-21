(()=>{
const visible=(p,i)=>!!p?.[i]&&(p[i].visibility??0)>.7&&p[i].x>.02&&p[i].x<.98&&p[i].y>.02&&p[i].y<.98;
function upperBody(p){
    // Pose landmarks cover the nose, both eyes, shoulders and elbows.
    // Hips, knees and feet are deliberately not used for this framing.
    if(![0,2,5,11,12,13,14].every(i=>visible(p,i)))return false;
    const shouldersY=(p[11].y+p[12].y)/2;
    const shouldersX=(p[11].x+p[12].x)/2;
    return p[0].y<shouldersY-.05&&Math.abs(p[11].x-p[12].x)>.12&&Math.abs(p[0].x-shouldersX)<.18;
}
function hand(p,side,raised){const wrist=side?16:15,shoulder=side?12:11;return visible(p,wrist)&&visible(p,shoulder)&&(raised?p[wrist].y<p[shoulder].y-.04:p[wrist].y>p[shoulder].y+.06);}
window.DaybreakScanPose={upperBody,hand};
})();
