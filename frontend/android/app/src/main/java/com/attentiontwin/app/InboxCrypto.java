package com.attentiontwin.app;
import android.security.keystore.*;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;

/** Separate non-authenticated Keystore key: reading the inbox never prompts for a screen lock. */
final class InboxCrypto {
    private static final String ALIAS="attentiontwin_inbox_v1";
    interface KeySource{SecretKey get() throws Exception;}
    static KeySource keys=InboxCrypto::androidKey;
    static synchronized SecretKey androidKey() throws Exception{KeyStore store=KeyStore.getInstance("AndroidKeyStore");store.load(null);if(store.containsAlias(ALIAS))return (SecretKey)store.getKey(ALIAS,null);KeyGenerator g=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");g.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setUserAuthenticationRequired(false).build());return g.generateKey();}
    static String encrypt(String text) throws Exception{Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,keys.get());return "v1:"+Base64.encodeToString(c.getIV(),Base64.NO_WRAP)+":"+Base64.encodeToString(c.doFinal(text.getBytes(StandardCharsets.UTF_8)),Base64.NO_WRAP);}
    static String decrypt(String text) throws Exception{String[] parts=text.split(":",3);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,keys.get(),new GCMParameterSpec(128,Base64.decode(parts[1],Base64.NO_WRAP)));return new String(c.doFinal(Base64.decode(parts[2],Base64.NO_WRAP)),StandardCharsets.UTF_8);}
}
