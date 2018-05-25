package usi.tdd.androidmutitest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Admin on 2016/8/24.
 */
public class DisplayTest {
    final int TOTAL_TOUCH = 7;
    int nTouchCount = 1;

    public void RunDisplay(final String strFunc, /*int nTouchCount,*/final AlertDialog.Builder dlg, final LinearLayout background, final MainActivity mainActivityThis) {
        switch (nTouchCount) {
            case 1:
                background.setBackgroundColor(Color.RED);
                Log.d("MFG_TEST", "DisplayTest Color = Red");
                break;
            case 2:
                background.setBackgroundColor(Color.GREEN);
                Log.d("MFG_TEST", "DisplayTest Color = Green");
                break;
            case 3:
                background.setBackgroundColor(Color.BLUE);
                Log.d("MFG_TEST", "DisplayTest Color = Blue");
                break;
            case 4:
                background.setBackgroundColor(Color.BLACK);
                Log.d("MFG_TEST", "DisplayTest Color = Black");
                break;
            case 5:
                background.setBackgroundColor(Color.WHITE);
                Log.d("MFG_TEST", "DisplayTest Color = White");
                break;
            case 6:
                ColorParent(background, mainActivityThis);
                Log.d("MFG_TEST", "DisplayTest Color = ColorParent");
                break;
            case 7:
                EndPopDialog(dlg, strFunc, "DisplayTest is OK ?", mainActivityThis);
                break;
        }

        if (nTouchCount < TOTAL_TOUCH + 1)
            nTouchCount++;
        background.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int nTouchEvent = event.getAction();
                switch (nTouchEvent) {
                    case MotionEvent.ACTION_DOWN:
                        if (strFunc.equals("FCT"))
                            RunDisplay(strFunc, dlg, background, mainActivityThis);
                        break;
                    //case MotionEvent.ACTION_UP:
                    //	break;
                }
                return false;
            }
        });
    }

    public void EndPopDialog(AlertDialog.Builder dlg, final String strFunc, String strSetMessage, final MainActivity mainActivityThis) {
        // TODO Auto-generated method stub
        boolean bTestResult = false;
        if (!strFunc.equals("BURNIN")) {
            dlg.setTitle("Message");
            dlg.setMessage(strSetMessage);

            dlg.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    Log.d("MFG_TEST", strFunc + " SUCCESSFUL TEST!!\r\n");
                    mainActivityThis.finish(); //<--�i��|�����D�A�����Factivity
                }
            });

            dlg.setNegativeButton("No", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    Log.d("MFG_TEST", strFunc + " UUT-FAIL\r\n");
                    mainActivityThis.finish(); //<--�i��|�����D
                }
            });
            dlg.show();
        }
    }

    private void ColorParent(LinearLayout background, MainActivity mainActivityThis) {
        background.setOrientation(LinearLayout.VERTICAL);
        TextView[] colorBar = new TextView[5];

        for (int nIdx = 0; nIdx < colorBar.length; nIdx++) {
            colorBar[nIdx] = new TextView(mainActivityThis);
            background.addView(colorBar[nIdx], nIdx, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        }

        int[] bla2w = {Color.BLACK, Color.WHITE};
        colorBar[0].setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, bla2w));

        int[] r2w = {Color.BLACK, Color.RED};
        colorBar[1].setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, r2w));

        int[] g2w = {Color.BLACK, Color.GREEN};
        colorBar[2].setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, g2w));

        int[] blu2w = {Color.BLACK, Color.BLUE};
        colorBar[3].setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, blu2w));

        int[] b2g = {Color.BLACK, Color.GRAY};
        colorBar[4].setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, b2g));

    }
}
