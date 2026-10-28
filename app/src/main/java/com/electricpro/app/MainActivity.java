package com.electricpro.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int navy = Color.rgb(4, 20, 39);
    private final int panel = Color.rgb(10, 43, 76);
    private final int panel2 = Color.rgb(13, 53, 91);
    private final int yellow = Color.rgb(255, 190, 35);
    private final int white = Color.rgb(245, 248, 255);
    private final int muted = Color.rgb(183, 204, 226);
    private final int green = Color.rgb(35, 190, 105);
    private LinearLayout root;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(navy);
        getWindow().setNavigationBarColor(navy);
        home();
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), Color.rgb(28, 75, 113)); return d;
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return t;
    }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w < 0 ? w : dp(w), h < 0 ? h : dp(h)); }

    private void page(String title, String subtitle, boolean back) {
        root = column(); root.setPadding(dp(18), dp(18), dp(18), dp(24)); root.setBackgroundColor(navy);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(navy); scroll.addView(root);
        setContentView(scroll);
        if (back) {
            TextView top = text("‹    " + title, 20, white, true); top.setPadding(0, dp(4), 0, dp(16));
            top.setOnClickListener(v -> home()); root.addView(top, lp(-1, -2));
        } else {
            TextView logo = text("⚡", 42, yellow, true); logo.setGravity(Gravity.CENTER); root.addView(logo, lp(-1, 54));
            TextView brand = text("ЭЛЕКТРИК ", 27, white, true); brand.setGravity(Gravity.CENTER);
            TextView pro = text("PRO", 27, yellow, true); pro.setGravity(Gravity.CENTER);
            LinearLayout name = new LinearLayout(this); name.setGravity(Gravity.CENTER);
            name.addView(brand); name.addView(pro); root.addView(name, lp(-1, 38));
        }
        if (subtitle != null && !subtitle.isEmpty()) {
            TextView sub = text(subtitle, 14, muted, false); sub.setGravity(back ? Gravity.START : Gravity.CENTER);
            sub.setPadding(0, dp(4), 0, dp(18)); root.addView(sub, lp(-1, -2));
        }
    }
    private void addSpace(int height) { View v = new View(this); root.addView(v, lp(1, height)); }
    private void actionButton(String label, Runnable action) {
        TextView b = text(label, 16, navy, true); b.setGravity(Gravity.CENTER); b.setPadding(dp(12), dp(13), dp(12), dp(13)); b.setBackground(bg(yellow, 12));
        LinearLayout.LayoutParams p = lp(-1, -2); p.topMargin = dp(8); p.bottomMargin = dp(5); root.addView(b, p); b.setOnClickListener(v -> action.run());
    }
    private void card(String icon, String title, String description, Runnable action) {
        LinearLayout c = column(); c.setPadding(dp(14), dp(15), dp(14), dp(15)); c.setBackground(bg(panel, 14));
        TextView i = text(icon, 27, yellow, true); i.setGravity(Gravity.CENTER); c.addView(i, lp(-1, 36));
        TextView t = text(title, 15, white, true); t.setGravity(Gravity.CENTER); t.setPadding(0, dp(7), 0, dp(3)); c.addView(t, lp(-1, -2));
        if (description != null && !description.isEmpty()) { TextView d = text(description, 12, muted, false); d.setGravity(Gravity.CENTER); c.addView(d, lp(-1, -2)); }
        c.setOnClickListener(v -> action.run());
    }
    private void addGridCardRow(String i1, String t1, String d1, Runnable r1, String i2, String t2, String d2, Runnable r2) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout a = new LinearLayout(this); a.setOrientation(LinearLayout.VERTICAL); a.addView(makeGridCard(i1,t1,d1,r1), new LinearLayout.LayoutParams(0, dp(132), 1));
        LinearLayout b = new LinearLayout(this); b.setOrientation(LinearLayout.VERTICAL); b.addView(makeGridCard(i2,t2,d2,r2), new LinearLayout.LayoutParams(0, dp(132), 1));
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, dp(132), 1); p1.rightMargin=dp(6);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, dp(132), 1); p2.leftMargin=dp(6);
        row.addView(a.getChildAt(0), p1); row.addView(b.getChildAt(0), p2);
        LinearLayout.LayoutParams rp=lp(-1,-2); rp.bottomMargin=dp(10); root.addView(row,rp);
    }
    private View makeGridCard(String icon,String title,String desc,Runnable run) {
        LinearLayout c=column(); c.setGravity(Gravity.CENTER); c.setPadding(dp(10),dp(10),dp(10),dp(10)); c.setBackground(bg(panel,14));
        TextView i=text(icon,28,yellow,true); i.setGravity(Gravity.CENTER); c.addView(i,lp(-1,38));
        TextView t=text(title,15,white,true); t.setGravity(Gravity.CENTER); c.addView(t,lp(-1,-2));
        TextView d=text(desc,11,muted,false); d.setGravity(Gravity.CENTER); d.setPadding(0,dp(4),0,0); c.addView(d,lp(-1,-2)); c.setOnClickListener(v->run.run()); return c;
    }
    private void infoCard(String title,String body) {
        LinearLayout c=column(); c.setPadding(dp(15),dp(14),dp(15),dp(14)); c.setBackground(bg(panel,13));
        c.addView(text(title,16,white,true)); TextView b=text(body,14,muted,false); b.setPadding(0,dp(7),0,0); c.addView(b);
        LinearLayout.LayoutParams p=lp(-1,-2);p.bottomMargin=dp(10);root.addView(c,p);
    }
    private EditText field(String label, String initial) {
        TextView l=text(label,13,muted,false); LinearLayout.LayoutParams lpp=lp(-1,-2);lpp.topMargin=dp(10);root.addView(l,lpp);
        EditText e=new EditText(this);e.setSingleLine(true);e.setText(initial);e.setTextColor(white);e.setHintTextColor(muted);e.setTextSize(16);e.setPadding(dp(12),dp(8),dp(12),dp(8));e.setBackground(bg(panel2,9));e.setInputType(8194);
        root.addView(e,lp(-1,48));return e;
    }
    private double value(EditText e) { try { return Double.parseDouble(e.getText().toString().trim().replace(',','.')); } catch(Exception ex) { return Double.NaN; } }
    private void resultCard(String title,String value,String note) {
        LinearLayout c=column();c.setPadding(dp(16),dp(16),dp(16),dp(16));c.setBackground(bg(panel,14));
        c.addView(text(title,16,white,true));TextView v=text(value,27,yellow,true);v.setPadding(0,dp(7),0,dp(5));c.addView(v);
        if(note!=null&&!note.isEmpty())c.addView(text(note,13,muted,false));LinearLayout.LayoutParams p=lp(-1,-2);p.topMargin=dp(14);root.addView(c,p);
    }
    private void home() {
        page("", "Ваш помощник в электромонтажных работах", false);
        TextView headline=text("Знания. Расчёты.\nБезопасность.",24,white,true);headline.setGravity(Gravity.CENTER);headline.setPadding(0,dp(12),0,dp(20));root.addView(headline,lp(-1,-2));
        addGridCardRow("▦","Калькулятор\nпроводов","Ток и сечение",()->calc(),"☼","Освещение","Световой поток",()->light());
        addGridCardRow("⬟","Безопасность","Правила и защита",()->safety(),"▤","Справочник","Формулы и таблицы",()->reference());
        actionButton("Начать",()->calc());
        addSpace(8); TextView footer=text("Работай безопасно! Работай с умом!",13,yellow,true);footer.setGravity(Gravity.CENTER);root.addView(footer,lp(-1,-2));
    }
    private void calc() {
        page("Калькулятор проводов","Ориентировочный расчёт электрической нагрузки",true);
        infoCard("Расчёт по току","Для однофазной нагрузки: I ≈ P / U. Для более точного расчёта учитывайте коэффициент мощности и тип нагрузки.");
        EditText power=field("Мощность нагрузки (Вт)","3500"); EditText voltage=field("Напряжение (В)","220");
        EditText cos=field("Коэффициент мощности cos φ (1 для простого расчёта)","1");
        actionButton("▦  Рассчитать ток",()->{
            double p=value(power),u=value(voltage),c=value(cos);
            if(!valid(p,u,c)||u<=0||c<=0||c>1){message("Введите корректные числа. Напряжение должно быть больше нуля, cos φ — от 0 до 1.");return;}
            double current=p/(u*c); resultCard("Расчётный ток",String.format(Locale.getDefault(),"%.2f А",current),"Это оценка нагрузки, а не готовый подбор кабеля или автомата.");
        });
        addSpace(12); infoCard("Падение напряжения","Падение зависит от материала, сечения, длины линии, тока и схемы сети. Для выбора кабеля используйте действующие нормы и таблицы производителя.");
        EditText amps=field("Ток нагрузки (А)","16"); EditText length=field("Длина линии в одну сторону (м)","20"); EditText section=field("Сечение медного проводника (мм²)","2.5");
        actionButton("Рассчитать падение напряжения",()->{
            double i=value(amps),len=value(length),s=value(section);if(!valid(i,len,s)||i<0||len<=0||s<=0){message("Введите положительные значения тока, длины и сечения.");return;}
            double drop=2*0.0175*len*i/s;double percent=drop/220*100;
            resultCard("Оценочное падение напряжения",String.format(Locale.getDefault(),"%.2f В (%.1f%%)",drop,percent),"Упрощённая оценка для медной однофазной линии при 220 В; не учитывает нагрев и реактивное сопротивление.");
        });
        infoCard("Важно","Нельзя выбирать сечение только по этому калькулятору. Учитываются способ прокладки, материал, температура, группировка кабелей, длина, защита и местные нормы.");
    }
    private boolean valid(double... vals){for(double v:vals)if(Double.isNaN(v)||Double.isInfinite(v))return false;return true;}
    private void light() {
        page("Освещение","Быстрый ориентировочный расчёт",true);
        EditText area=field("Площадь помещения (м²)","20");EditText lux=field("Требуемая освещённость (лк)","300");EditText watts=field("Мощность одного светильника (Вт)","12");EditText lm=field("Световой поток одного светильника (лм)","1200");
        actionButton("☼  Рассчитать освещение",()->{
            double a=value(area),l=value(lux),w=value(watts),flux=value(lm);if(!valid(a,l,w,flux)||a<=0||l<=0||w<=0||flux<=0){message("Все значения должны быть больше нуля.");return;}
            double needed=a*l;int count=(int)Math.ceil(needed/flux);resultCard("Ориентировочно светильников",String.valueOf(count),String.format(Locale.getDefault(),"Суммарный поток: %.0f лм\nОриентировочная мощность: %.0f Вт",count*flux,count*w));
        });
        infoCard("Примечание","Расчёт не учитывает коэффициент использования, высоту монтажа, загрязнение и отражение поверхностей. Для рабочего проекта требуется светотехнический расчёт.");
    }
    private void safety() {
        page("Безопасность","Основные правила перед началом работ",true);
        infoCard("⚠  Отключите питание","Отключите нужную цепь, исключите случайное повторное включение и обозначьте место работ.");
        infoCard("✓  Проверьте отсутствие напряжения","Используйте исправный двухполюсный указатель и проверьте его работу до и после проверки цепи.");
        infoCard("⬟  Используйте СИЗ","Подбирайте защитные очки, перчатки, инструмент и одежду под конкретную работу и оценённый риск.");
        infoCard("＋  Первая помощь","При поражении током сначала безопасно отключите питание. Не касайтесь пострадавшего, пока источник опасности не устранён. Вызовите экстренную помощь.");
        infoCard("⛨  Защитные устройства","Автоматы, УЗО, заземление и сечения проводников выбирают по проекту, условиям установки и действующим нормам.");
        infoCard("Важно","Не выполняйте работы под напряжением без специальной квалификации, допуска и соответствующих процедур.");
    }
    private void reference() {
        page("Справочник","Базовые формулы и справочные значения",true);
        infoCard("Закон Ома","I = U / R\nТок (А) равен напряжению (В), делённому на сопротивление (Ом).");
        infoCard("Электрическая мощность","P = U × I\nДля переменного тока и реальной нагрузки учитывайте cos φ: P ≈ U × I × cos φ.");
        infoCard("Сопротивление","R = U / I\nСопротивление измеряется в омах (Ω).");
        infoCard("Падение напряжения","Для простой однофазной медной линии ориентировочно: ΔU ≈ 2 × 0,0175 × L × I / S, где L — длина в одну сторону (м), I — ток (А), S — сечение (мм²).");
        infoCard("Сечение кабеля — не выбирайте только по току","Допустимый ток зависит от способа прокладки, изоляции, температуры, количества нагруженных жил, группировки, материала и защитного аппарата. Используйте действующие таблицы ПУЭ/местных норм и данные производителя.");
        infoCard("Цвета проводников","Защитный PE обычно жёлто-зелёный; нейтральный N — голубой. Цвета и назначение необходимо проверять по схеме и нормам конкретной установки, не полагаться только на цвет.");
    }
    private void message(String s){new AlertDialog.Builder(this).setMessage(s).setPositiveButton("Понятно",null).show();}
}
