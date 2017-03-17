package com.gongzetao.loop.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.gongzetao.loop.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by baixinping on 2016/9/7.
 */
public class DialogUtils {
    OnButtonClickListener onButtonClickListener;
    OnDateChange onDateChange;
    ImageView iv_cancel;
    public EditText ev_value;
    public long longDate;
    public String strDate;
    public RadioButton rb_sex;
    private TextView tvChageHint;

    public void setOnDateChange(OnDateChange onDateChange) {
        this.onDateChange = onDateChange;
    }

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    public void openUpdateDialog(Context context) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        final AlertDialog dialog = dialogBuilder.create();
        View view = View.inflate(context, R.layout.layout_update_dialog_item, null);
        ev_value = (EditText) view.findViewById(R.id.update_dialog_item_et_value);
        iv_cancel = (ImageView) view.findViewById(R.id.update_dialog_item_bt_cancel);
        tvChageHint = (TextView)view.findViewById(R.id.tv_chage_hint);

        TextView tv_ok = (TextView) view.findViewById(R.id.update_dialog_item_bt_ok);
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onButtonClickListener != null)
                    onButtonClickListener.clickOk();
                dialog.dismiss();
            }
        });

        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(view);
        dialog.show();
        InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        im.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public  void setTvChageHintText(String text) {
        tvChageHint.setText(text);
    }

    public void openDateSelectDialog(Context context, int year, int month, int day) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        final AlertDialog dialog = dialogBuilder.create();

        View view = View.inflate(context, R.layout.layout_date_select_dialog_item, null);
        ev_value = (EditText) view.findViewById(R.id.update_dialog_item_et_value);
        DatePicker picker = (DatePicker) view.findViewById(R.id.date_select_item_dp);
        picker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthOfYear, dayOfMonth);
                longDate = calendar.getTimeInMillis();
                SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
                strDate = format.format(calendar.getTime());
                if (onDateChange != null)
                    onDateChange.dateChange();
            }
        });
        dialog.setView(view);
        dialog.show();
    }

    public void openSexSelectDialog(Context context, int state) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        final AlertDialog dialog = dialogBuilder.create();
        final View view = View.inflate(context, R.layout.dialog_update_sex, null);
        RadioGroup rg_sex = (RadioGroup) view.findViewById(R.id.dialog_sex_rg);
        if (state == 0){

        }
        rg_sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.dialog_sex_rb_men)
                    rb_sex = (RadioButton) view.findViewById(R.id.dialog_sex_rb_men);
                else
                    rb_sex = (RadioButton) view.findViewById(R.id.dialog_sex_rb_women);
                if (onDateChange != null)
                    onDateChange.dateChange();
            }
        });
        dialog.setView(view);
        dialog.show();
    }

    public interface OnButtonClickListener {
        void clickCancel();

        void clickOk();
    }

    public interface OnDateChange {
        void dateChange();
    }
}
