package com.ark.android.onlinesourcelib;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.ark.android.gallerylib.data.GallaryDataBaseContract;

/**
 *
 * Created by ahmed-basyouni on 4/25/17.
 */

public class FivePxSyncUtils {
    private static final long SYNC_FREQUENCY = 60 * 60 * 24;  // 1 hour (in seconds)
    private static final String CONTENT_AUTHORITY = GallaryDataBaseContract.GALLERY_AUTHORITY;
    private static final String PREF_SETUP_COMPLETE = "fivePx_setup_complete";
    // Value below must match the account type specified in res/xml/fivepx_syncadapteradapter.xml
    public static final String ACCOUNT_TYPE = "com.ark.android.arkwallpaperfivehundred.account";

    /**
     * Create an entry for this application in the system account list, if it isn't already there.
     *
     * @param context Context
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static boolean CreateSyncAccount(Context context, Bundle bundle) {
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

        // Create account, if it's missing. (Either first run, or user has deleted account.)
        Account account = FivePxGenericAccountService.GetAccount(ACCOUNT_TYPE);
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(account, null, null)) {
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            ContentResolver.addPeriodicSync(
                    account, CONTENT_AUTHORITY, bundle,SYNC_FREQUENCY);
            newAccount = true;
        }

        // Schedule an initial sync if we detect problems with either our account or our local
        // data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
        // the account list, so wee need to check both.)
        if (newAccount || !setupComplete) {
            TriggerRefresh(bundle);
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }

        return newAccount;
    }

    public static void updatePeriodicSync(String oldCat, String newCat){
        Account account = TumblrGenericAccountService.GetAccount(ACCOUNT_TYPE);
        Bundle bundle = new Bundle();
        bundle.putString(FiveHundredSyncAdapter.CAT_KEY , oldCat);
        bundle.putBoolean("isPer", true);
        ContentResolver.removePeriodicSync(account, CONTENT_AUTHORITY, bundle);
        Bundle newBundle = new Bundle();
        newBundle.putString(FiveHundredSyncAdapter.CAT_KEY , newCat);
        newBundle.putBoolean("isPer", true);
        ContentResolver.addPeriodicSync(
                account, CONTENT_AUTHORITY, newBundle,SYNC_FREQUENCY);
    }


    /**
     * Helper method to trigger an immediate sync ("refresh").
     *
     * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refresh" button.
     *
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     * @param bundle
     */
    public static void TriggerRefresh(Bundle bundle) {
        Bundle b = new Bundle();
        b.putAll(bundle);
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                FivePxGenericAccountService.GetAccount(ACCOUNT_TYPE), // Sync account
                CONTENT_AUTHORITY,                 // Content authority
                b);                                             // Extras
    }
}
