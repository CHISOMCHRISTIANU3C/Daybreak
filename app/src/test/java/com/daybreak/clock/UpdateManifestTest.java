package com.daybreak.clock;
import org.junit.Test;
import org.json.JSONObject;
import static org.junit.Assert.*;

public class UpdateManifestTest {
    private JSONObject manifest()throws Exception{return new JSONObject().put("packageName","com.daybreak.clock").put("versionCode",6).put("versionName","0.6.0").put("size",12000000).put("minSdk",30).put("apkUrl","https://raw.githubusercontent.com/CHISOMCHRISTIANU3C/Daybreak/main/updates/Daybreak-v0.6.apk").put("sha256","a".repeat(64));}
    @Test public void acceptsOwnRepository()throws Exception{assertEquals(6,new UpdateManifest(manifest().toString()).versionCode);}
    @Test public void rejectsHttp()throws Exception{JSONObject o=manifest().put("apkUrl","http://raw.githubusercontent.com/CHISOMCHRISTIANU3C/Daybreak/main/updates/test.apk");assertThrows(Exception.class,()->new UpdateManifest(o.toString()));}
    @Test public void rejectsOtherOwner()throws Exception{JSONObject o=manifest().put("apkUrl","https://raw.githubusercontent.com/attacker/Daybreak/main/updates/test.apk");assertThrows(Exception.class,()->new UpdateManifest(o.toString()));}
    @Test public void rejectsOtherApp()throws Exception{JSONObject o=manifest().put("packageName","other.app");assertThrows(Exception.class,()->new UpdateManifest(o.toString()));}
    @Test public void rejectsInvalidDigest()throws Exception{JSONObject o=manifest().put("sha256","bad");assertThrows(Exception.class,()->new UpdateManifest(o.toString()));}
    @Test public void rejectsOversizedDownload()throws Exception{JSONObject o=manifest().put("size",UpdateManifest.MAX_BYTES+1);assertThrows(Exception.class,()->new UpdateManifest(o.toString()));}
    @Test public void rejectsFractionalVersion()throws Exception{JSONObject o=manifest().put("versionCode",6.5);assertThrows(Exception.class,()->new UpdateManifest(o.toString()));}
    @Test public void rejectsCredentialUrls(){assertFalse(UpdateManifest.trustedArtifact("https://user:pass@raw.githubusercontent.com/CHISOMCHRISTIANU3C/Daybreak/main/updates/a.apk"));}
    @Test public void rejectsUntrustedRedirect(){assertFalse(UpdateManifest.trustedDownload("https://github.com.attacker.invalid/file.apk"));}
    @Test public void permitsGithubReleaseRedirect(){assertTrue(UpdateManifest.trustedDownload("https://release-assets.githubusercontent.com/file.apk"));}
}
