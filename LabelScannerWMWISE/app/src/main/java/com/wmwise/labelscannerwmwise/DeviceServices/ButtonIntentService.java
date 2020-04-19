package com.wmwise.labelscannerwmwise.DeviceServices;

import android.app.IntentService;
import android.content.Intent;

import com.panasonic.toughpad.android.api.appbtn.AppButtonManager;
import com.wmwise.labelscannerwmwise.CorrectionScanActivity;
import com.wmwise.labelscannerwmwise.MassiveAssignationActivity;
import com.wmwise.labelscannerwmwise.MenuActivity;
import com.wmwise.labelscannerwmwise.PickAndLoadingActivity;
import com.wmwise.labelscannerwmwise.ScanBarCodeActivity;
import com.wmwise.labelscannerwmwise.ScanTrackingActivity;

public class ButtonIntentService extends IntentService {
    public ButtonIntentService() {
        super("Button Intent Handler Thread");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!intent.getAction().equals(AppButtonManager.ACTION_APPBUTTON)) {
            // Ignore..
            return;
        }

        if (PickAndLoadingActivity.getInstance() != null){
            PickAndLoadingActivity.getInstance().updateButtonState(intent);
        }
        else if (CorrectionScanActivity.getInstance() != null) {
            CorrectionScanActivity.getInstance().updateButtonState(intent);
        }
        else if (ScanBarCodeActivity.getInstance() != null) {
            ScanBarCodeActivity.getInstance().updateButtonState(intent);
        }
        else if(ScanTrackingActivity.getInstance() != null) {
            ScanTrackingActivity.getInstance().updateButtonState(intent);
        }
        else if (MassiveAssignationActivity.getInstance() != null) {
            MassiveAssignationActivity.getInstance().updateButtonState(intent);
        }
        else if (ButtonTriggerActivity.getInstance() != null) {
            ButtonTriggerActivity.getInstance().updateButtonState(intent);
        }
        else {
            Intent launchIntent = new Intent(getBaseContext(), MenuActivity.class);
            launchIntent.setAction(Intent.ACTION_MAIN);
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            getApplication().startActivity(launchIntent);
        }

    }
}
