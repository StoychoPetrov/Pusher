package eu.mobile.fashionpoint;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import java.util.Date;

public class CallReceiver extends PhonecallReceiver {

    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        super.onIncomingCallStarted(ctx, number, start);
        ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("phone: ", number);
        clipboard.setPrimaryClip(clip);
    }
}