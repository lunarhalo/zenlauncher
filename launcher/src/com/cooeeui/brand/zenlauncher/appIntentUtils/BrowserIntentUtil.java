
package com.cooeeui.brand.zenlauncher.appIntentUtils;

import java.util.ArrayList;
import java.util.List;

import com.cooeeui.brand.zenlauncher.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class BrowserIntentUtil {
    private Context context = null;
    private List<ResolveInfo> listInfo = null;
    private Button positiveButton = null;
    private Button negativeButton = null;
    private int intentposition = -1;
    public Intent browserIntent = null;

    public BrowserIntentUtil(Context context) {
        this.context = context;
    }

    @SuppressLint("NewApi")
    public void createDialog() {
        AlertDialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle(context.getResources().getString(R.string.chooseApp));
        builder.setNegativeButton(context.getResources().getString(R.string.always),
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        browserIntent = getIntentByPosition();
                        context.startActivity(browserIntent);
                    }

                });
        builder.setPositiveButton(context.getResources().getString(R.string.once),
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Intent intent = getIntentByPosition();
                        context.startActivity(intent);
                    }
                });
        ListView listView = new ListView(context);
        findAllbrowserApp();
        final MyBrowserAdapter adapter = new MyBrowserAdapter(listInfo, context);
        listView.setAdapter(adapter);
        builder.setView(listView);
        dialog = builder.create();
        dialog.show();// 必现要先show以后，获得的positiveButton和negativeButton才不会为null
        positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        positiveButton.setEnabled(false);
        negativeButton.setEnabled(false);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                intentposition = position;

                if (parent != null) {
                    setBackgroundColorView(parent, position);
                }
            }
        });
    }

    private Intent getIntentByPosition() {
        Intent intent = null;
        if (listInfo != null && listInfo.size() > intentposition) {
            ResolveInfo info = listInfo.get(intentposition);
            String pkgName = info.activityInfo.packageName;
            String clsName = info.activityInfo.name;
            ComponentName cp = new ComponentName(pkgName, clsName);
            intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(cp);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }
        return intent;
    }

    private void setBackgroundColorView(ViewGroup parent, int position) {
        // TODO Auto-generated method stub
        for (int i = 0; i < parent.getChildCount(); i++) {
            View viewChild = parent.getChildAt(i);
            if (i == position) {
                viewChild.setBackgroundColor(0xff1aa3d2);
            } else {
                viewChild.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        positiveButton.setEnabled(true);
        negativeButton.setEnabled(true);
    }

    boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * 查找手机中所有的
     */
    private void findAllbrowserApp() {
        // TODO Auto-generated method stub
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://"));
        if (listInfo != null) {
            listInfo.clear();
            listInfo = null;
        }

        listInfo = new ArrayList<ResolveInfo>();
        List<ResolveInfo> list = pm.queryIntentActivities(intent,
                PackageManager.GET_INTENT_FILTERS);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            String pkgName = info.activityInfo.packageName;
            String clsName = info.activityInfo.name;
            ComponentName cp = new ComponentName(pkgName, clsName);
            intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(cp);
            if (isIntentAvailable(context, intent)) {
                listInfo.add(info);
            }
        }
    }

    private class MyBrowserAdapter extends BaseAdapter {
        private ImageButton image = null;
        private TextView textView = null;
        private List<ResolveInfo> listInfo = null;
        private Context context = null;
        private PackageManager pm = null;

        public MyBrowserAdapter(List<ResolveInfo> listInfo, Context context) {
            this.listInfo = listInfo;
            this.context = context;
            pm = context.getPackageManager();
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return listInfo.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.browser_adapter_layout,
                        null);
                image = (ImageButton) convertView.findViewById(R.id.browser_iamge);
                textView = (TextView) convertView.findViewById(R.id.browser_text);

                ResolveInfo info = listInfo.get(position);
                image.setBackgroundDrawable(info.loadIcon(pm));
                textView.setText(info.loadLabel(pm).toString());
            }
            return convertView;
        }

    }
}
