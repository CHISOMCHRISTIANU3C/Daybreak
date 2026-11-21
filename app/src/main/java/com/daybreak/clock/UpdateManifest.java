package com.daybreak.clock;
import org.json.*;
import java.net.URI;

final class UpdateManifest {
    static final String OWNER="CHISOMCHRISTIANU3C", REPOSITORY="Daybreak";
    static final String MANIFEST_URL="https://raw.githubusercontent.com/"+OWNER+"/"+REPOSITORY+"/main/updates/update.json";
    static final long MAX_BYTES=100L*1024*1024;
    final long versionCode,size;final int minSdk;final String versionName,apkUrl,sha256,notes;
    UpdateManifest(String json)throws Exception{
        JSONObject o=new JSONObject(json);if(!"com.daybreak.clock".equals(o.getString("packageName")))throw new IllegalArgumentException("This update is for a different app.");
        if(!o.get("versionCode").toString().matches("[0-9]+"))throw new IllegalArgumentException("Invalid update version.");
        versionCode=o.getLong("versionCode");size=o.getLong("size");minSdk=o.getInt("minSdk");versionName=o.getString("versionName");apkUrl=o.getString("apkUrl");sha256=o.getString("sha256").toLowerCase();notes=o.optString("notes","");
        if(versionCode<1||size<1||size>MAX_BYTES||minSdk<1||versionName.isBlank()||versionName.length()>60||notes.length()>3000||!sha256.matches("[0-9a-f]{64}")||!trustedArtifact(apkUrl))throw new IllegalArgumentException("The update information is invalid.");
    }
    static boolean trustedArtifact(String url){try{URI u=new URI(url);if(!safeHttps(u))return false;String path=u.getPath();return u.getHost().equals("raw.githubusercontent.com")&&path.startsWith("/"+OWNER+"/"+REPOSITORY+"/main/updates/")&&path.endsWith(".apk")||u.getHost().equals("github.com")&&path.startsWith("/"+OWNER+"/"+REPOSITORY+"/releases/download/")&&path.endsWith(".apk");}catch(Exception e){return false;}}
    static boolean safeHttps(URI u){return "https".equals(u.getScheme())&&u.getUserInfo()==null&&u.getHost()!=null&&(u.getPort()==-1||u.getPort()==443)&&u.getFragment()==null;}
    static boolean trustedDownload(String url){try{URI u=new URI(url);return safeHttps(u)&&java.util.Set.of("raw.githubusercontent.com","github.com","release-assets.githubusercontent.com","objects.githubusercontent.com").contains(u.getHost());}catch(Exception e){return false;}}
}
