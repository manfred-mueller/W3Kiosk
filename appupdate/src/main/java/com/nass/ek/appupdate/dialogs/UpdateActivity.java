package com.nass.ek.appupdate.dialogs;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.fragment.app.Fragment;

import com.nass.ek.appupdate.R;
import com.nass.ek.appupdate.base.AbstractUpdateActivity;
import com.nass.ek.appupdate.services.VersionModel;
import com.nass.ek.appupdate.utils.Constant;
import com.nass.ek.appupdate.utils.PackageUtils;

public class UpdateActivity extends AbstractUpdateActivity implements DownloadDialog.OnFragmentOperation {

    protected VersionModel mModel;
    protected String mToastMsg;
    protected String mDownloadDialogText;
    protected boolean mIsShowToast;
    protected boolean mIsShowBackgroundDownload;
    private int notificationIcon;

    private String mUpdateTitle;
    private String mUpdateContentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        getWindow().setLayout(calcWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
        setFinishOnTouchOutside(false);
        notificationIcon = getIntent().getIntExtra(Constant.NOTIFICATION_ICON, 0);
        mModel = (VersionModel) getIntent().getSerializableExtra(Constant.MODEL);
        mToastMsg = getIntent().getStringExtra(Constant.TOAST_MSG);
        mIsShowToast = getIntent().getBooleanExtra(Constant.IS_SHOW_TOAST_MSG, true);
        mIsShowBackgroundDownload = getIntent().getBooleanExtra(Constant.IS_SHOW_BACKGROUND_DOWNLOAD, true);
        mDownloadDialogText = getIntent().getStringExtra(Constant.DOWNLOAD_DIALOG_HEADER_TEXT);
        mUpdateTitle = getIntent().getStringExtra(Constant.UPDATE_TITLE);
        mUpdateContentText = getIntent().getStringExtra(Constant.UPDATE_CONTENT_TEXT);
        if (mModel == null) {
            finish();
            return;
        }

        showUpdateDialog();
    }

    private int calcWidth() {
        if (getResources().getBoolean(R.bool.au_is_tablet)) {
            return getResources().getDimensionPixelSize(R.dimen.au_dialog_max_width);
        } else {
            WindowManager wm = getWindow().getWindowManager();
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            final int windowWidth = size.x;

            final int windowHorizontalPadding = getResources().getDimensionPixelSize(R.dimen.au_dialog_horizontal_margin);
            return windowWidth - (windowHorizontalPadding * 2);
        }
    }

    private void showUpdateDialog() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, getUpdateDialogFragment())
                .commit();
    }

    public void showDownLoadProgress() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, getDownLoadDialogFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (PackageUtils.getVersionCode(getApplicationContext()) < mModel.getMinSupport()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected Fragment getUpdateDialogFragment() {
        if (mUpdateTitle != null && !mUpdateTitle.isEmpty()) {

            if (mUpdateContentText != null && !mUpdateContentText.isEmpty())
                return UpdateDialog.newInstance(mModel, mToastMsg, mIsShowToast, mUpdateTitle, mUpdateContentText);

            return UpdateDialog.newInstance(mModel, mToastMsg, mIsShowToast, mUpdateTitle);
        }

        return UpdateDialog.newInstance(mModel, mToastMsg, mIsShowToast);
    }

    @Override
    protected Fragment getDownLoadDialogFragment() {
        return DownloadDialog.newInstance(mModel.getUrl(), notificationIcon, PackageUtils.getVersionCode(getApplicationContext()) < mModel.getMinSupport(), mIsShowBackgroundDownload, mDownloadDialogText);
    }

    @Override
    public void onFailed() {
        showUpdateDialog();
    }
}