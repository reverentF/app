package jp.ac.titech.itpro.sdl.tsuyoso2;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by kayo on 2016/10/31.
 */
public class PlaceholderFragment extends Fragment implements
    OnDateClickListener, OnNextBackClickListener{
    public PlaceholderFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container,
                false);
        //カレンダービューの取得
        CalendarView calendarView = (CalendarView) rootView
                .findViewById(R.id.calendar);
        //カレンダーに日付をセット
        //第一引数：表示する年
        //第二引数：表示する月
        //第三引数：カレンダーの縦の長さを可変にするか true、６行固定にするか false
        //第四引数：前月(≪)、翌月（≫）ボタンを表示するか true
        calendarView.set(MonthlyCalendar.today(MonthlyCalendar.TODAY_YEAR),
                MonthlyCalendar.today(MonthlyCalendar.TODAY_MONTH), true,
                true);
        calendarView.setOnDateClickListener(this);
        calendarView.setOnNextBackClickListener(this);
        return rootView;
    }

    /**
     * @param year
     *            month　day クリックされた、年月日。
     */
    @Override
    public void onDateClick(int year, int month, int day) {
        Toast.makeText(
                getActivity(),
                Integer.toString(year) + "-" + Integer.toString(month)
                        + "-" + Integer.toString(day), Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * @param year
     *            month year クリックした、前月（≪）　または　翌月（≫）　の、年と月がセットされる。
     *
     * @param nextback
     *            MonthlyCalendar.BACK_MONTH(前月　≪　が押された) or
     *            MonthlyCalendar.NEXT_MONTH（翌月　≫　が押された）
     */
    @Override
    public void onNextBackClick(int year, int month, int nextback) {
        Toast.makeText(
                getActivity(),
                Integer.toString(year) + "-" + Integer.toString(month)
                        + "----" + Integer.toString(nextback),
                Toast.LENGTH_SHORT).show();
    }

}
