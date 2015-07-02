package com.padeoe.officex86installer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;

/**
 * Created by Kangkang on 2015/5/23.
 */
public class AboutDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.dialog, null);
        builder.setView(view)
                .setPositiveButton(R.string.feedback, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                        emailIntent.setType("plain/text");
                        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{(String)getResources().getText(R.string.author_email)});
                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,(String)getResources().getText(R.string.feedback_email_title));
                        getActivity().startActivity(Intent.createChooser(emailIntent,getResources().getText(R.string.send_email)));
                    }
                })
                .setNegativeButton(R.string.like, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getActivity(),getResources().getText(R.string.thankyou), Toast.LENGTH_SHORT).show();
                        AVObject Like = new AVObject("Like");
                        Like.put("hello", "x");
                        Like.saveInBackground();
                    }
                });
        TextView versionTextView=(TextView)view.findViewById(R.id.version);
        TextView authorTextView=(TextView)view.findViewById(R.id.author);
        authorTextView.setText(getResources().getText(R.string.author)+(String) getResources().getText(R.string.author_email));
        try{
            String versionName=this.getActivity().getPackageManager()
                    .getPackageInfo(this.getActivity().getPackageName(), 0).versionName;
            versionTextView.setText(getResources().getText(R.string.version)+versionName);
        }
        catch(PackageManager.NameNotFoundException e){

        }
        return builder.create();
    }
}
