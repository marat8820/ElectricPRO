package com.electricpro.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int navy = Color.rgb(4, 20, 39);
    private final int panel = Color.rgb(10, 43, 76);
    private final int panel2 = Color.rgb(13, 53, 91);
    private final int yellow = Color.rgb(255, 190, 35);
    private final int white = Color.rgb(245, 248, 255);
    private final int muted = Color.rgb(183, 204, 226);
    private LinearLayout root;
    private boolean onHome = true;
    private final List<Order> orders = new ArrayList<>();
    private static final String PREFS_NAME = "electricpro_orders";
    private static final String ORDERS_KEY = "orders_json";

    private static class Line {
        String name;
        double quantity;
        double price;
        Line(String name, double quantity, double price) {
            this.name = name; this.quantity = quantity; this.price = price;
        }
        double total() { return quantity * price; }
    }
    private static class Order {
        String name;
        String executorDetails = "";
        String customerDetails = "";
        String address = "";
        final List<Line> works = new ArrayList<>();
        final List<Line> materials = new ArrayList<>();
        Order(String name) { this.name = name; }
        double workTotal() { double s = 0; for (Line l : works) s += l.total(); return s; }
        double materialTotal() { double s = 0; for (Line l : materials) s += l.total(); return s; }
        double total() { return workTotal() + materialTotal(); }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(navy);
        getWindow().setNavigationBarColor(navy);
        loadOrders();
        home();
    }

    @Override @SuppressWarnings("deprecation") public void onBackPressed() {
        if (!onHome) home(); else super.onBackPressed();
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), Color.rgb(28, 75, 113)); return d;
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView v = new TextView(this); v.setText(value); v.setTextSize(size); v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return v;
    }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(w < 0 ? w : dp(w), h < 0 ? h : dp(h));
    }
    private void page(String title, String subtitle, boolean back) {
        onHome = !back; root = column(); root.setPadding(dp(18), dp(18), dp(18), dp(24)); root.setBackgroundColor(navy);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(navy); scroll.addView(root); setContentView(scroll);
        if (back) {
            TextView top = text("‹    " + title, 20, white, true); top.setPadding(0, dp(4), 0, dp(16));
            top.setOnClickListener(v -> home()); root.addView(top, lp(-1, -2));
        } else {
            TextView logo = text("⚡", 42, yellow, true); logo.setGravity(Gravity.CENTER); root.addView(logo, lp(-1, 54));
            TextView brand = text("ЭЛЕКТРИК ", 27, white, true); brand.setGravity(Gravity.CENTER);
            TextView pro = text("PRO", 27, yellow, true); pro.setGravity(Gravity.CENTER);
            LinearLayout name = new LinearLayout(this); name.setGravity(Gravity.CENTER); name.addView(brand); name.addView(pro); root.addView(name, lp(-1, 38));
        }
        if (subtitle != null && !subtitle.isEmpty()) {
            TextView sub = text(subtitle, 14, muted, false); sub.setGravity(back ? Gravity.START : Gravity.CENTER);
            sub.setPadding(0, dp(4), 0, dp(18)); root.addView(sub, lp(-1, -2));
        }
    }
    private void addSpace(int h) { View v = new View(this); root.addView(v, lp(1, h)); }
    private void actionButton(String label, Runnable action) {
        TextView b = text(label, 16, navy, true); b.setGravity(Gravity.CENTER); b.setPadding(dp(12), dp(13), dp(12), dp(13)); b.setBackground(bg(yellow, 12));
        LinearLayout.LayoutParams p = lp(-1, -2); p.topMargin = dp(8); p.bottomMargin = dp(5); root.addView(b, p); b.setOnClickListener(v -> action.run());
    }
   addGridCardRow(
    private View makeGridCard(String icon, String title, String description, Runnable action) {
        LinearLayout c = column(); c.setGravity(Gravity.CENTER); c.setPadding(dp(10),dp(10),dp(10),dp(10)); c.setBackground(bg(panel,14));
        TextView iv=text(icon,28,yellow,true); iv.setGravity(Gravity.CENTER); c.addView(iv,lp(-1,38));
        TextView tv=text(title,15,white,true); tv.setGravity(Gravity.CENTER); c.addView(tv,lp(-1,-2));
        TextView dv=text(description,11,muted,false); dv.setGravity(Gravity.CENTER); dv.setPadding(0,dp(4),0,0); c.addView(dv,lp(-1,-2));
       c.setOnClickListener(v -> {
    if (action != null) {
        action.run();
    }
}); return c;
    }
    private void infoCard(String title, String body) {
        LinearLayout c=column(); c.setPadding(dp(15),dp(14),dp(15),dp(14)); c.setBackground(bg(panel,13)); c.addView(text(title,16,white,true));
        TextView b=text(body,14,muted,false); b.setPadding(0,dp(7),0,0); c.addView(b);
        LinearLayout.LayoutParams p=lp(-1,-2); p.bottomMargin=dp(10); root.addView(c,p);
    }
    private EditText field(String label, String initial) {
        TextView l=text(label,13,muted,false); LinearLayout.LayoutParams p=lp(-1,-2); p.topMargin=dp(10); root.addView(l,p);
        EditText e=new EditText(this); e.setSingleLine(true); e.setText(initial); e.setTextColor(white); e.setHintTextColor(muted); e.setTextSize(16);
        e.setPadding(dp(12),dp(8),dp(12),dp(8)); e.setBackground(bg(panel2,9));
        e.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        root.addView(e,lp(-1,48)); return e;
    }
    private double value(EditText e) { try { return Double.parseDouble(e.getText().toString().trim().replace(',','.')); } catch(Exception ex) { return Double.NaN; } }
    private boolean valid(double... values) { for(double v:values) if(Double.isNaN(v)||Double.isInfinite(v)) return false; return true; }
    private void resultCard(String title, String result, String note) {
        LinearLayout c=column(); c.setPadding(dp(16),dp(16),dp(16),dp(16)); c.setBackground(bg(panel,14)); c.addView(text(title,16,white,true));
        TextView r=text(result,27,yellow,true); r.setPadding(0,dp(7),0,dp(5)); c.addView(r); if(note!=null&&!note.isEmpty()) c.addView(text(note,13,muted,false));
        LinearLayout.LayoutParams p=lp(-1,-2); p.topMargin=dp(14); root.addView(c,p);
    }
   private void home() {
    page("", "", false);
    TextView subtitle = text(
        "Знания. Расчёты. Безопасность.",
        16, white, true
    );
    subtitle.setGravity(Gravity.CENTER);
    subtitle.setPadding(dp(8), 0, dp(8), dp(20));
    root.addView(subtitle, lp(-1, -2));

    addGridCardRow(
        "▦", "Калькулятор\nпроводов", "Ток и сечение",
        () -> calc(),
        "☼", "Освещение", "Световой поток",
        () -> light()
    );

    addSpace(10);

    addGridCardRow(
        "⬟", "Безопасность", "Правила и защита",
        () -> safety(),
        "▤", "Справочник", "Формулы и таблицы",
        () -> reference()
    );

    addSpace(14);

addGridCardRow(
    "▣", "Мои заказы", "Заказы и работы",
    () -> ordersPage(),
    "⚡", "Расширенные расчёты", "Дополнительные формулы",
    () -> advancedCalc()
);
addSpace(10);

addView(makeGridCard(
    "▤",
    "Редактор щита",
    "Компоновка, схема и проверка",
    () -> panelEditor()
), lp(-1, dp(130)));

addSpace(20);

    TextView f = text(
        "РАБОТАЙ БЕЗОПАСНО!\nРаботай с умом.",
        14, yellow, true
    );
    f.setGravity(Gravity.CENTER);
    f.setPadding(dp(8), dp(12), dp(8), dp(20));
    root.addView(f, lp(-1, -2));
}
    
private void panelEditor() {
    page("Редактор щита", "Компоновка оборудования и схема", true);

    TextView title = text("НОВЫЙ ЭЛЕКТРИЧЕСКИЙ ЩИТ", 20, yellow, true);
    title.setGravity(Gravity.CENTER);
    title.setPadding(dp(8), dp(16), dp(8), dp(12));
    root.addView(title, lp(-1, -2));

    TextView info = text(
        "Выбери режим работы. Проект пока демонстрационный.",
        14, white, false
    );
    info.setGravity(Gravity.CENTER);
    info.setPadding(dp(8), 0, dp(8), dp(16));
    root.addView(info, lp(-1, -2));

    actionButton("▦  Компоновка щита", () -> panelLayout());
    addSpace(10);

    actionButton("⌁  Однолинейная схема", () -> panelScheme());
    addSpace(10);

    actionButton("＋  Добавить оборудование", () -> panelAddEquipment());
    addSpace(10);

    actionButton("▤  Список оборудования", () -> panelEquipmentList());
}


private void panelLayout() {
    page("Редактор щита", "Выберите оборудование и добавьте его на DIN-рейку", true);

    LinearLayout editor = new LinearLayout(this);
    editor.setOrientation(LinearLayout.VERTICAL);
    editor.setPadding(dp(12), dp(12), dp(12), dp(12));
    editor.setBackgroundColor(Color.rgb(232, 236, 241));
    root.addView(editor, lp(-1, -2));

    TextView title = text("КАТАЛОГ ОБОРУДОВАНИЯ", 15,
            Color.rgb(25, 40, 58), true);
    editor.addView(title, lp(-1, -2));

    LinearLayout catalog = new LinearLayout(this);
    catalog.setOrientation(LinearLayout.HORIZONTAL);
    editor.addView(catalog, lp(-1, -2));

    String[] devices = {
        "Вводной\nавтомат",
        "УЗО",
        "Дифавтомат",
        "Реле\nнапряжения"
    };

    String[] symbols = {"ВА", "УЗО", "АВДТ", "РН"};

    for (int i = 0; i < devices.length; i++) {
        final String device = devices[i];

        TextView item = text(
                symbols[i] + "\n\n" + devices[i],
                12, Color.rgb(25, 40, 58), true);

        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(5), dp(12), dp(5), dp(12));

        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.WHITE);
        shape.setCornerRadius(dp(8));
        shape.setStroke(dp(1), Color.rgb(170, 180, 190));
        item.setBackground(shape);

        LinearLayout.LayoutParams itemParams =
                new LinearLayout.LayoutParams(0, dp(90), 1);
        itemParams.setMargins(dp(3), dp(8), dp(3), dp(8));
        catalog.addView(item, itemParams);

        item.setOnClickListener(v -> {
            addDeviceToBoard(device);
        });
    }

    TextView boardTitle = text("РАБОЧЕЕ ПОЛЕ", 15,
            Color.rgb(25, 40, 58), true);
    LinearLayout.LayoutParams titleParams = lp(-1, -2);
    titleParams.topMargin = dp(14);
    editor.addView(boardTitle, titleParams);

    LinearLayout board = new LinearLayout(this);
    board.setOrientation(LinearLayout.VERTICAL);
    board.setPadding(dp(12), dp(16), dp(12), dp(16));

    GradientDrawable boardShape = new GradientDrawable();
    boardShape.setColor(Color.rgb(250, 250, 247));
    boardShape.setCornerRadius(dp(8));
    boardShape.setStroke(dp(2), Color.rgb(115, 125, 135));
    board.setBackground(boardShape);

    editor.addView(board, lp(-1, -2));

    TextView railLabel = text("DIN-РЕЙКА № 1", 12,
            Color.rgb(70, 80, 90), true);
    board.addView(railLabel, lp(-1, -2));

    LinearLayout rail = new LinearLayout(this);
    rail.setOrientation(LinearLayout.HORIZONTAL);
    rail.setGravity(Gravity.CENTER_VERTICAL);
    rail.setPadding(dp(4), dp(8), dp(4), dp(8));

    GradientDrawable railShape = new GradientDrawable();
    railShape.setColor(Color.rgb(205, 211, 217));
    railShape.setCornerRadius(dp(4));
    railShape.setStroke(dp(1), Color.rgb(125, 135, 145));
    rail.setBackground(railShape);

    LinearLayout.LayoutParams railParams = lp(-1, -2);
    railParams.topMargin = dp(8);
    board.addView(rail, railParams);

    TextView empty = text(
            "Выберите устройство выше",
            13, Color.rgb(90, 100, 110), false);
    empty.setGravity(Gravity.CENTER);
    empty.setPadding(0, dp(18), 0, dp(18));
    board.addView(empty, lp(-1, -2));

    // Храним ссылки на рабочее поле и рейку для добавления устройств.
    editor.setTag("shield_editor");
    board.setTag("shield_board");
    rail.setTag("shield_rail");
    empty.setTag("shield_empty");

    actionButton("＋  Добавить оборудование",
            () -> panelAddEquipment());
}

private void addDeviceToBoard(String deviceName) {
    if (root == null) return;

    LinearLayout rail = root.findViewWithTag("shield_rail");
    TextView empty = root.findViewWithTag("shield_empty");

    if (rail == null) {
        android.widget.Toast.makeText(this, "Сначала откройте редактор щита", android.widget.Toast.LENGTH_SHORT).show();
        return;
    }

    if (empty != null) {
        empty.setVisibility(View.GONE);
    }

    TextView device = text(
            deviceName + "\n16 A",
            12,
            Color.rgb(25, 40, 58),
            true
    );

    device.setGravity(Gravity.CENTER);
    device.setPadding(dp(5), dp(10), dp(5), dp(10));

    GradientDrawable shape = new GradientDrawable();
    shape.setColor(Color.WHITE);
    shape.setCornerRadius(dp(4));
    shape.setStroke(dp(2), Color.rgb(60, 85, 110));
    device.setBackground(shape);

    LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                    dp(76), dp(82));

    params.setMargins(dp(2), 0, dp(2), 0);
    rail.addView(device, params);

    device.setOnClickListener(v -> {
        new AlertDialog.Builder(this)
                .setTitle(deviceName)
                .setItems(
                        new String[]{
                                "Изменить номинал",
                                "Удалить устройство"
                        },
                        (dialog, which) -> {
                            if (which == 0) {
                                String[] ratings = {
                                        "6 A", "10 A", "16 A",
                                        "20 A", "25 A", "32 A",
                                        "40 A", "63 A"
                                };

                                new AlertDialog.Builder(this)
                                        .setTitle("Выберите номинал")
                                        .setItems(ratings,
                                                (d, selected) -> {
                                                    device.setText(
                                                            deviceName + "\n" +
                                                            ratings[selected]);
                                                })
                                        .show();
                            } else {
                                rail.removeView(device);

                                if (rail.getChildCount() == 1
                                        && empty != null) {
                                    empty.setVisibility(View.VISIBLE);
                                }
                            }
                        })
                .show();
    });
}

private void panelScheme() {
    page("Однолинейная схема", "Предварительный макет", true);

    TextView diagram = text(
        "ПИТАНИЕ 230 В\n" +
        "     │\n" +
        "[Вводной автомат]\n" +
        "     │\n" +
        "    [УЗО]\n" +
        "     │\n" +
        " ┌───┴───┐\n" +
        " │       │\n" +
        "[АВ 1]  [АВ 2]\n" +
        " │       │\n" +
        "Линия 1  Линия 2",
        18, white, true
    );
    diagram.setGravity(Gravity.CENTER);
    diagram.setPadding(dp(12), dp(24), dp(12), dp(24));
    root.addView(diagram, lp(-1, -2));

    TextView warning = text(
        "Учебный макет, не готовый проект для монтажа.",
        13, yellow, true
    );
    warning.setGravity(Gravity.CENTER);
    root.addView(warning, lp(-1, -2));
}

private void panelAddEquipment() {
    final String[] equipment = {
        "Вводной автомат",
        "Автоматический выключатель",
        "УЗО",
        "Дифавтомат",
        "Контактор",
        "Реле напряжения",
        "Счётчик",
        "Клеммник"
    };

    new android.app.AlertDialog.Builder(this)
        .setTitle("Выбери оборудование")
        .setItems(equipment, (dialog, which) -> {
            android.widget.Toast.makeText(
                this,
                "Выбрано: " + equipment[which] +
                ". Добавление в проект будет реализовано следующим шагом.",
                android.widget.Toast.LENGTH_LONG
            ).show();
        })
        .setNegativeButton("Отмена", null)
        .show();
}

private void panelEquipmentList() {
    page("Список оборудования", "Состав проекта", true);

    TextView info = text(
        "Пока оборудование не добавлено.\n\n" +
        "Открой «Добавить оборудование», чтобы выбрать аппарат.",
        16, white, false
    );
    info.setGravity(Gravity.CENTER);
    info.setPadding(dp(12), dp(24), dp(12), dp(24));
    root.addView(info, lp(-1, -2));

    actionButton("＋  Добавить оборудование", () -> panelAddEquipment());
}

    private void calc() {
        page("Калькулятор проводов","Ориентировочный расчёт электрической нагрузки",true);
        infoCard("Расчёт по току","Для однофазной нагрузки: I ≈ P / U. Для более точного расчёта учитывайте коэффициент мощности и тип нагрузки.");
        EditText power=field("Мощность нагрузки (Вт)","3500"), voltage=field("Напряжение (В)","220"), cos=field("Коэффициент мощности cos φ","1");
        actionButton("▦  Рассчитать ток",()->{ double p=value(power),u=value(voltage),c=value(cos); if(!valid(p,u,c)||p<=0||u<=0||c<=0||c>1){message("Введите корректные числа: мощность и напряжение больше нуля, cos φ — от 0 до 1.");return;} resultCard("Расчётный ток",String.format(Locale.getDefault(),"%.2f А",p/(u*c)),"Оценка нагрузки, а не готовый подбор кабеля или автомата."); });
        addSpace(12); infoCard("Падение напряжения","Учитываются материал, сечение, длина линии, ток, схема сети и условия монтажа.");
        EditText amps=field("Ток нагрузки (А)","16"), length=field("Длина в одну сторону (м)","20"), section=field("Сечение медного проводника (мм²)","2.5");
        actionButton("Рассчитать падение напряжения",()->{double a=value(amps),l=value(length),s=value(section);if(!valid(a,l,s)||a<0||l<=0||s<=0){message("Введите корректные значения.");return;}double d=2*.0175*l*a/s;resultCard("Оценочное падение напряжения",String.format(Locale.getDefault(),"%.2f В (%.1f%%)",d,d/220*100),"Упрощённая оценка для медной однофазной линии при 220 В.");});
        infoCard("Важно","Не выбирайте сечение только по этому калькулятору. Учитывайте способ прокладки, температуру, группировку, защиту и действующие нормы.");
    }
    private void light() {
        page("Освещение","Быстрый ориентировочный расчёт",true);
        EditText area=field("Площадь помещения (м²)","20"), lux=field("Освещённость (лк)","300"), watts=field("Мощность светильника (Вт)","12"), lumens=field("Световой поток светильника (лм)","1200");
        actionButton("☼  Рассчитать освещение",()->{double a=value(area),l=value(lux),w=value(watts),f=value(lumens);if(!valid(a,l,w,f)||a<=0||l<=0||w<=0||f<=0){message("Все значения должны быть больше нуля.");return;}int n=(int)Math.ceil(a*l/f);resultCard("Ориентировочно светильников",String.valueOf(n),String.format(Locale.getDefault(),"Суммарный поток: %.0f лм\nМощность: %.0f Вт",n*f,n*w));});
        infoCard("Примечание","Расчёт не учитывает коэффициент использования, высоту монтажа, загрязнение и отражение поверхностей.");
    }
    private void advancedCalc() {
    page("Расширенные расчёты", "Электротехнический калькулятор", true);

    actionButton("Ток по мощности", () -> calcCurrent());
    actionButton("Мощность по току", () -> calcPower());
    actionButton("Падение напряжения в кабеле", () -> calcVoltageDrop());
    }
    
private void calcCurrent() {
    final android.widget.EditText power = new android.widget.EditText(this);
    power.setHint("Мощность, Вт");
    power.setInputType(8194);

    final android.widget.EditText voltage = new android.widget.EditText(this);
    voltage.setHint("Напряжение, В");
    voltage.setInputType(8194);

    android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
    layout.setOrientation(android.widget.LinearLayout.VERTICAL);
    layout.setPadding(dp(20), dp(8), dp(20), dp(8));
    layout.addView(power);
    layout.addView(voltage);

    new android.app.AlertDialog.Builder(this)
        .setTitle("Расчёт тока")
        .setView(layout)
        .setPositiveButton("Рассчитать", (dialog, which) -> {
            try {
                double p = Double.parseDouble(power.getText().toString().trim());
                double u = Double.parseDouble(voltage.getText().toString().trim());

                if (p <= 0 || u <= 0) throw new Exception();

                double current = p / u;
                new android.app.AlertDialog.Builder(this)
                    .setTitle("Результат")
                    .setMessage(String.format(java.util.Locale.getDefault(),
                        "Расчётный ток: %.2f А\n\nФормула: I = P / U",
                        current))
                    .setPositiveButton("OK", null)
                    .show();
            } catch (Exception e) {
                new android.app.AlertDialog.Builder(this)
                    .setMessage("Введи корректные значения больше нуля.")
                    .setPositiveButton("OK", null)
                    .show();
            }
        })
        .setNegativeButton("Отмена", null)
        .show();
}

private void calcPower() {
    final android.widget.EditText current = new android.widget.EditText(this);
    current.setHint("Ток, А");
    current.setInputType(8194);

    final android.widget.EditText voltage = new android.widget.EditText(this);
    voltage.setHint("Напряжение, В");
    voltage.setInputType(8194);

    android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
    layout.setOrientation(android.widget.LinearLayout.VERTICAL);
    layout.setPadding(dp(20), dp(8), dp(20), dp(8));
    layout.addView(current);
    layout.addView(voltage);

    new android.app.AlertDialog.Builder(this)
        .setTitle("Расчёт мощности")
        .setView(layout)
        .setPositiveButton("Рассчитать", (dialog, which) -> {
            try {
                double i = Double.parseDouble(current.getText().toString().trim());
                double u = Double.parseDouble(voltage.getText().toString().trim());

                if (i <= 0 || u <= 0) throw new Exception();

                double p = i * u;
                new android.app.AlertDialog.Builder(this)
                    .setTitle("Результат")
                    .setMessage(String.format(java.util.Locale.getDefault(),
                        "Расчётная мощность: %.2f Вт\n(%.2f кВт)\n\nФормула: P = U × I",
                        p, p / 1000.0))
                    .setPositiveButton("OK", null)
                    .show();
            } catch (Exception e) {
                new android.app.AlertDialog.Builder(this)
                    .setMessage("Введи корректные значения больше нуля.")
                    .setPositiveButton("OK", null)
                    .show();
            }
        })
        .setNegativeButton("Отмена", null)
        .show();
}

private void calcVoltageDrop() {
    final android.widget.EditText length = new android.widget.EditText(this);
    length.setHint("Длина кабеля в одну сторону, м");
    length.setInputType(8194);

    final android.widget.EditText current = new android.widget.EditText(this);
    current.setHint("Ток, А");
    current.setInputType(8194);

    final android.widget.EditText section = new android.widget.EditText(this);
    section.setHint("Сечение медной жилы, мм²");
    section.setInputType(8194);

    android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
    layout.setOrientation(android.widget.LinearLayout.VERTICAL);
    layout.setPadding(dp(20), dp(8), dp(20), dp(8));
    layout.addView(length);
    layout.addView(current);
    layout.addView(section);

    new android.app.AlertDialog.Builder(this)
        .setTitle("Падение напряжения")
        .setMessage("Предварительный расчёт для медного кабеля, однофазная цепь 230 В.")
        .setView(layout)
        .setPositiveButton("Рассчитать", (dialog, which) -> {
            try {
                double l = Double.parseDouble(length.getText().toString().trim());
                double i = Double.parseDouble(current.getText().toString().trim());
                double s = Double.parseDouble(section.getText().toString().trim());

                if (l <= 0 || i <= 0 || s <= 0) throw new Exception();

                double drop = 2 * l * i * 0.0175 / s;
                double percent = drop / 230.0 * 100.0;

                new android.app.AlertDialog.Builder(this)
                    .setTitle("Результат")
                    .setMessage(String.format(java.util.Locale.getDefault(),
                        "Падение напряжения: %.2f В\n" +
                        "Относительное падение: %.2f %%\n" +
                        "Ориентировочное напряжение на нагрузке: %.2f В\n\n" +
                        "Расчёт приблизительный. Он не проверяет допустимый ток кабеля, " +
                        "условия прокладки и требования применимых норм.",
                        drop, percent, 230.0 - drop))
                    .setPositiveButton("OK", null)
                    .show();
            } catch (Exception e) {
                new android.app.AlertDialog.Builder(this)
                    .setMessage("Введи корректные значения больше нуля.")
                    .setPositiveButton("OK", null)
                    .show();
            }
        })
        .setNegativeButton("Отмена", null)
        .show();
}

    private void safety() {
        page("Безопасность","Основные правила перед началом работ",true);
        infoCard("⚠  Отключите питание","Отключите нужную цепь, исключите случайное повторное включение и обозначьте место работ.");
        infoCard("✓  Проверьте отсутствие напряжения","Используйте исправный двухполюсный указатель и проверьте его работу до и после проверки цепи.");
        infoCard("⬟  Используйте СИЗ","Подбирайте защитные очки, перчатки, инструмент и одежду под конкретную работу и оценённый риск.");
        infoCard("＋  Первая помощь","Сначала безопасно отключите питание. Не касайтесь пострадавшего, пока источник опасности не устранён. Вызовите экстренную помощь.");
        infoCard("⛨  Защитные устройства","Автоматы, УЗО, заземление и сечения проводников выбирают по проекту, условиям установки и действующим нормам.");
    }
    private void reference() {
        page("Справочник","Базовые формулы и справочные значения",true);
        infoCard("Закон Ома","I = U / R\nТок равен напряжению, делённому на сопротивление.");
        infoCard("Электрическая мощность","P = U × I\nДля реальной нагрузки переменного тока учитывайте cos φ.");
        infoCard("Падение напряжения","Для простой однофазной медной линии: ΔU ≈ 2 × 0,0175 × L × I / S.");
        infoCard("Цвета проводников","PE обычно жёлто-зелёный; N — голубой. Проверяйте назначение по схеме и нормам, не только по цвету.");
    }

    // Заказы: работы и материалы хранятся отдельно.
    private void loadOrders() {
        orders.clear();
        String raw=getSharedPreferences(PREFS_NAME,MODE_PRIVATE).getString(ORDERS_KEY,"[]");
        try {
            JSONArray a=new JSONArray(raw);
            for(int i=0;i<a.length();i++){
                JSONObject o=a.getJSONObject(i); Order order=new Order(o.optString("name","Новый заказ"));
                order.executorDetails=o.optString("executorDetails",""); order.customerDetails=o.optString("customerDetails",""); order.address=o.optString("address","");
                readLines(o.optJSONArray("lines"),order.works); // совместимость со старой версией
                readLines(o.optJSONArray("works"),order.works);
                readLines(o.optJSONArray("materials"),order.materials);
                orders.add(order);
            }
        } catch(Exception ignored){orders.clear();}
    }
    private void readLines(JSONArray a,List<Line> target) {
        if(a==null)return;
        for(int i=0;i<a.length();i++)try{JSONObject x=a.getJSONObject(i);target.add(new Line(x.optString("name",""),x.optDouble("quantity",0),x.optDouble("price",0)));}catch(Exception ignored){}
    }
    private JSONArray linesJson(List<Line> lines) throws Exception {
        JSONArray a=new JSONArray(); for(Line l:lines){JSONObject x=new JSONObject();x.put("name",l.name);x.put("quantity",l.quantity);x.put("price",l.price);a.put(x);} return a;
    }
    private void saveOrders() {
        JSONArray a=new JSONArray();
        try { for(Order o:orders){JSONObject x=new JSONObject();x.put("name",o.name);x.put("executorDetails",o.executorDetails);x.put("customerDetails",o.customerDetails);x.put("address",o.address);x.put("works",linesJson(o.works));x.put("materials",linesJson(o.materials));a.put(x);} }
        catch(Exception e){message("Не удалось сохранить заказы.");return;}
        getSharedPreferences(PREFS_NAME,MODE_PRIVATE).edit().putString(ORDERS_KEY,a.toString()).apply();
    }
    private String money(double a){return String.format(Locale.getDefault(),"%,.2f",a).replace('\u00a0',' ');}
    private String number(double a){return Math.abs(a-Math.rint(a))<.000001?String.format(Locale.getDefault(),"%.0f",a):String.format(Locale.getDefault(),"%.2f",a);}
    private void ordersPage() {
        page("Мои заказы","Сметы на работы и материалы",true);
        actionButton("＋  Создать заказ",()->orderEditor(new Order("Новый заказ"),-1));
        if(orders.isEmpty()){addSpace(12);infoCard("Пока нет заказов","Создайте заказ, добавьте работы и материалы. Стоимость строк и общий итог рассчитываются автоматически.");return;}
        for(int i=0;i<orders.size();i++){
            final int idx=i;Order o=orders.get(i);LinearLayout c=column();c.setPadding(dp(14),dp(14),dp(14),dp(14));c.setBackground(bg(panel,13));
            c.addView(text(o.name,18,white,true));c.addView(text("Работ: "+o.works.size()+" • Материалов: "+o.materials.size(),13,muted,false));
            TextView total=text("Итого: "+money(o.total()),20,yellow,true);total.setPadding(0,dp(8),0,dp(6));c.addView(total);
            LinearLayout row=new LinearLayout(this);TextView edit=smallButton("Открыть / изменить"),del=smallButton("Удалить");
            LinearLayout.LayoutParams p1=new LinearLayout.LayoutParams(0,dp(44),1);p1.rightMargin=dp(5);row.addView(edit,p1);
            LinearLayout.LayoutParams p2=new LinearLayout.LayoutParams(0,dp(44),1);p2.leftMargin=dp(5);row.addView(del,p2);
            edit.setOnClickListener(v->orderEditor(orders.get(idx),idx));
            del.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Удалить заказ?").setMessage("Заказ «"+orders.get(idx).name+"» будет удалён.")
                .setNegativeButton("Отмена",null).setPositiveButton("Удалить",(d,w)->{orders.remove(idx);saveOrders();ordersPage();}).show());
            c.addView(row);LinearLayout.LayoutParams cp=lp(-1,-2);cp.bottomMargin=dp(10);root.addView(c,cp);
        }
    }
    private TextView smallButton(String label){TextView b=text(label,13,navy,true);b.setGravity(Gravity.CENTER);b.setPadding(dp(5),dp(6),dp(5),dp(6));b.setBackground(bg(yellow,9));return b;}
    private void orderEditor(Order o,int existingIndex) {
        page("Заказ","Отдельный расчёт стоимости работ и материалов",true);
        EditText orderName=field("Название сметы / объекта",o.name),executor=field("Исполнитель: имя, телефон, реквизиты",o.executorDetails),
                customer=field("Заказчик: имя, телефон, реквизиты",o.customerDetails),address=field("Адрес объекта",o.address);
        actionButton("＋  Добавить строку работ",()->{copyFields(o,orderName,executor,customer,address);lineDialog(o,false,-1);});
        LinearLayout worksBox=column();root.addView(worksBox,lp(-1,-2));
        TextView workTotal=text("",18,white,true);root.addView(workTotal,lp(-1,-2));
        actionButton("＋  Добавить материал",()->{copyFields(o,orderName,executor,customer,address);lineDialog(o,true,-1);});
        LinearLayout materialsBox=column();root.addView(materialsBox,lp(-1,-2));
        TextView materialTotal=text("",18,white,true);root.addView(materialTotal,lp(-1,-2));
        TextView grand=text("",23,yellow,true);grand.setPadding(0,dp(12),0,dp(12));root.addView(grand,lp(-1,-2));
        Runnable refresh=()->{
            fillLineCards(worksBox,o,false,()->orderEditor(o,findOrderIndex(o)));
            fillLineCards(materialsBox,o,true,()->orderEditor(o,findOrderIndex(o)));
            workTotal.setText("ИТОГО РАБОТ: "+money(o.workTotal()));
            materialTotal.setText("ИТОГО МАТЕРИАЛОВ: "+money(o.materialTotal()));
            grand.setText("ИТОГО ЗА ЗАКАЗ: "+money(o.total()));
        };
        refresh.run();
        actionButton("▤  Создать PDF сметы",()->{
            copyFields(o,orderName,executor,customer,address);
            if(o.works.isEmpty()&&o.materials.isEmpty()){message("Добавьте хотя бы одну строку работ или материалов.");return;}
            if(o.name.isEmpty()){message("Сначала введите название заказа.");return;}
            createEstimatePdf(o);
        });
        actionButton("Сохранить заказ",()->{
            copyFields(o,orderName,executor,customer,address);
            if(o.name.isEmpty()){message("Введите название заказа или объекта.");return;}
            persistOrder(o,existingIndex);
        });
    }
    private void copyFields(Order o,EditText name,EditText executor,EditText customer,EditText address){
        o.name=name.getText().toString().trim();o.executorDetails=executor.getText().toString().trim();
        o.customerDetails=customer.getText().toString().trim();o.address=address.getText().toString().trim();
    }
    private void fillLineCards(LinearLayout box,Order o,boolean material,Runnable reopen){
        box.removeAllViews();List<Line> lines=material?o.materials:o.works;
        TextView heading=text(material?"МАТЕРИАЛЫ":"РАБОТЫ",15,yellow,true);heading.setPadding(0,dp(8),0,dp(5));box.addView(heading);
        if(lines.isEmpty()){TextView empty=text(material?"Материалы пока не добавлены.":"Работы пока не добавлены.",13,muted,false);box.addView(empty);}
        for(int i=0;i<lines.size();i++){
            final int idx=i;Line line=lines.get(i);LinearLayout c=column();c.setPadding(dp(12),dp(12),dp(12),dp(12));c.setBackground(bg(panel,11));
            c.addView(text(line.name,16,white,true));c.addView(text("Количество: "+number(line.quantity),13,muted,false));
            c.addView(text("Цена за единицу: "+money(line.price),13,muted,false));
            c.addView(text("Сумма: "+money(line.total()),17,yellow,true));
            LinearLayout row=new LinearLayout(this);TextView edit=smallButton("Изменить"),del=smallButton("Удалить");
            LinearLayout.LayoutParams p1=new LinearLayout.LayoutParams(0,dp(40),1);p1.rightMargin=dp(5);row.addView(edit,p1);
            LinearLayout.LayoutParams p2=new LinearLayout.LayoutParams(0,dp(40),1);p2.leftMargin=dp(5);row.addView(del,p2);
            edit.setOnClickListener(v->lineDialog(o,material,idx));del.setOnClickListener(v->new AlertDialog.Builder(this)
                .setTitle("Удалить строку?").setMessage(line.name).setNegativeButton("Отмена",null)
                .setPositiveButton("Удалить",(d,w)->{lines.remove(idx);orderEditor(o,findOrderIndex(o));}).show());
            c.addView(row);LinearLayout.LayoutParams cp=lp(-1,-2);cp.bottomMargin=dp(7);box.addView(c,cp);
        }
    }
    private void persistOrder(Order o,int existingIndex){
        if(existingIndex>=0&&existingIndex<orders.size())orders.set(existingIndex,o);else orders.add(o);
        saveOrders();message("Заказ «"+o.name+"» сохранён.\nРаботы: "+money(o.workTotal())+"\nМатериалы: "+money(o.materialTotal())+"\nИтого: "+money(o.total()));ordersPage();
    }
    private int findOrderIndex(Order o){for(int i=0;i<orders.size();i++)if(orders.get(i)==o)return i;return -1;}
    private void lineDialog(Order o,boolean material,int index){
        List<Line> lines=material?o.materials:o.works;Line old=index>=0&&index<lines.size()?lines.get(index):null;
        LinearLayout layout=column();layout.setPadding(dp(18),dp(4),dp(18),dp(4));
        EditText name=dialogField(material?"Название материала":"Название работ",old==null?"":old.name);
        EditText quantity=dialogField("Количество или длина",old==null?"1":number(old.quantity));
        EditText price=dialogField(material?"Цена материала за единицу":"Цена работы за единицу",old==null?"0":number(old.price));
        TextView calculated=text("Сумма: 0.00",18,yellow,true);calculated.setPadding(0,dp(10),0,dp(4));
        layout.addView(name,lp(-1,48));layout.addView(quantity,lp(-1,48));layout.addView(price,lp(-1,48));layout.addView(calculated);
        Runnable update=()->{double q=parseNumber(quantity.getText().toString()),p=parseNumber(price.getText().toString());
            calculated.setText(valid(q,p)&&q>=0&&p>=0?"Сумма: "+money(q*p):"Сумма: —");};
        TextWatcher watcher=new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){update.run();}public void afterTextChanged(Editable e){}};
        quantity.addTextChangedListener(watcher);price.addTextChangedListener(watcher);update.run();
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(old==null?(material?"Новый материал":"Новая строка работ"):"Изменить строку")
            .setView(layout).setNegativeButton("Отмена",null).setPositiveButton("Сохранить",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view->{
            String n=name.getText().toString().trim();double q=parseNumber(quantity.getText().toString()),p=parseNumber(price.getText().toString());
            if(n.isEmpty()){message(material?"Введите название материала.":"Введите название работ.");return;}
            if(!valid(q,p)||q<=0||p<0){message("Количество должно быть больше нуля, цена не может быть отрицательной.");return;}
            Line l=new Line(n,q,p);if(index>=0&&index<lines.size())lines.set(index,l);else lines.add(l);
            dialog.dismiss();orderEditor(o,findOrderIndex(o));
        }));
        dialog.show();
    }
    private EditText dialogField(String label,String initial){
        EditText e=new EditText(this);e.setSingleLine(true);e.setText(initial);e.setHint(label);e.setTextColor(white);e.setHintTextColor(muted);e.setTextSize(16);
        e.setPadding(dp(12),dp(8),dp(12),dp(8));e.setBackground(bg(panel2,9));
        if(label.startsWith("Название"))e.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        else e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        LinearLayout.LayoutParams p=lp(-1,48);p.topMargin=dp(5);p.bottomMargin=dp(5);return e;
    }
    private double parseNumber(String raw){try{if(raw==null||raw.trim().isEmpty())return Double.NaN;return Double.parseDouble(raw.trim().replace(',','.'));}catch(Exception e){return Double.NaN;}}

    private void createEstimatePdf(Order order) {
        PdfDocument doc=new PdfDocument();Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        final int W=595,H=842,left=42,right=553;PdfDocument.Page page=null;Canvas canvas=null;int y=0,pageNo=0;
        try {
            pageNo++;page=doc.startPage(new PdfDocument.PageInfo.Builder(W,H,pageNo).create());canvas=page.getCanvas();y=48;
            y=pdfHeader(canvas,p,order,left,y);
            y=drawPdfLines(doc,page,canvas,p,order,order.works,false,left,right,y,pageNo);
            // После возможного перехода страницы возвращаем текущую страницу/номер через отдельный универсальный рендер ниже.
            doc.finishPage(page);page=null;
            // Формируем отдельные аккуратные страницы для материалов и итогов, чтобы таблицы не обрезались.
            if(!order.materials.isEmpty()){
                pageNo++;page=doc.startPage(new PdfDocument.PageInfo.Builder(W,H,pageNo).create());canvas=page.getCanvas();y=48;
                p.setColor(navy);p.setTextSize(19);p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));canvas.drawText("ЭЛЕКТРИК PRO — СМЕТА",left,y,p);y+=32;
                p.setColor(Color.BLACK);p.setTextSize(14);canvas.drawText("МАТЕРИАЛЫ",left,y,p);y+=25;
                y=drawTableOnPage(canvas,p,order.materials,left,right,y);
                y+=24;p.setTextSize(13);p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
                canvas.drawText("Итого материалов: "+money(order.materialTotal()),left,y,p);
                doc.finishPage(page);page=null;
            }
            pageNo++;page=doc.startPage(new PdfDocument.PageInfo.Builder(W,H,pageNo).create());canvas=page.getCanvas();y=70;
            p.setColor(navy);p.setTextSize(20);p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));canvas.drawText("ИТОГОВАЯ СТОИМОСТЬ",left,y,p);y+=40;
            p.setColor(Color.BLACK);p.setTextSize(14);p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
            canvas.drawText("Работы: "+money(order.workTotal()),left,y,p);y+=25;
            canvas.drawText("Материалы: "+money(order.materialTotal()),left,y,p);y+=32;
            p.setTextSize(18);p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));canvas.drawText("ИТОГО ЗА ЗАКАЗ: "+money(order.total()),left,y,p);y+=65;
            p.setTextSize(10);p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
            canvas.drawText("Исполнитель: ____________________________",left,y,p);canvas.drawText("Заказчик: ______________________________",left+270,y,p);y+=32;
            canvas.drawText("Подпись: _________________________________",left,y,p);canvas.drawText("Подпись: ________________________________",left+270,y,p);y+=30;
            canvas.drawText("Документ сформирован приложением Электрик PRO",left,y,p);
            doc.finishPage(page);page=null;
            String fileName=safeFileName(order.name)+"_"+new SimpleDateFormat("yyyyMMdd_HHmm",Locale.getDefault()).format(new Date())+".pdf";
            OutputStream out;
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                ContentValues values=new ContentValues();values.put(MediaStore.Downloads.DISPLAY_NAME,fileName);values.put(MediaStore.Downloads.MIME_TYPE,"application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/ElectricPRO");
                Uri uri=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,values);if(uri==null)throw new java.io.IOException("Не удалось создать PDF-файл");
                out=getContentResolver().openOutputStream(uri);
            }else{
                File dir=getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);if(dir==null)throw new java.io.IOException("Папка документов недоступна");
                if(!dir.exists()&&!dir.mkdirs())throw new java.io.IOException("Не удалось создать папку документов");out=new FileOutputStream(new File(dir,fileName));
            }
            if(out==null)throw new java.io.IOException("Не удалось открыть PDF-файл");doc.writeTo(out);out.close();
            message("PDF-смета создана: "+fileName+"\nРаботы: "+money(order.workTotal())+"\nМатериалы: "+money(order.materialTotal())+"\nИтого: "+money(order.total())+
                (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q?"\nПапка: Загрузки/ElectricPRO":"\nПапка документов приложения"));
        }catch(Exception e){message("Не удалось создать PDF-смету: "+e.getMessage());}
        finally{if(page!=null)try{doc.finishPage(page);}catch(Exception ignored){}doc.close();}
    }
    private int pdfHeader(Canvas c,Paint p,Order o,int left,int y){
        p.setColor(navy);p.setTextSize(22);p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));c.drawText("ЭЛЕКТРИК PRO",left,y,p);y+=28;
        p.setColor(Color.rgb(30,60,90));p.setTextSize(17);c.drawText("СМЕТА НА РАБОТЫ И МАТЕРИАЛЫ",left,y,p);y+=28;
        p.setColor(Color.BLACK);p.setTextSize(11);p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        c.drawText("Заказ: "+clipText(o.name,75),left,y,p);y+=18;c.drawText("Дата: "+new SimpleDateFormat("dd.MM.yyyy",Locale.getDefault()).format(new Date()),left,y,p);y+=18;
        if(!o.executorDetails.isEmpty()){c.drawText("Исполнитель: "+clipText(o.executorDetails,75),left,y,p);y+=16;}
        if(!o.customerDetails.isEmpty()){c.drawText("Заказчик: "+clipText(o.customerDetails,75),left,y,p);y+=16;}
        if(!o.address.isEmpty()){c.drawText("Адрес: "+clipText(o.address,75),left,y,p);y+=16;}
        y+=12;p.setTextSize(14);p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));c.drawText("РАБОТЫ",left,y,p);y+=24;
        return drawTableOnPage(c,p,o.works,left,553,y);
    }
    private int drawPdfLines(PdfDocument doc,PdfDocument.Page page,Canvas c,Paint p,Order o,List<Line> lines,boolean materials,int left,int right,int y,int pageNo){return y;}
    private int drawTableOnPage(Canvas c,Paint p,List<Line> lines,int left,int right,int y){
        p.setColor(Color.BLACK);p.setTextSize(10);p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        c.drawText("№",left,y,p);c.drawText("Наименование",left+25,y,p);c.drawText("Кол-во",330,y,p);c.drawText("Цена",400,y,p);c.drawText("Сумма",475,y,p);y+=8;c.drawLine(left,y,right,y,p);y+=20;
        p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        for(int i=0;i<lines.size();i++){
            if(y>760){p.setTextSize(9);c.drawText("Продолжение таблицы сметы см. в приложении.",left,y,p);break;}
            Line l=lines.get(i);p.setTextSize(10);c.drawText(String.valueOf(i+1),left,y,p);c.drawText(clipText(l.name,35),left+25,y,p);
            c.drawText(number(l.quantity),330,y,p);c.drawText(money(l.price),400,y,p);c.drawText(money(l.total()),475,y,p);y+=34;
            p.setColor(Color.LTGRAY);c.drawLine(left,y-12,right,y-12,p);p.setColor(Color.BLACK);
        }
        return y;
    }
    private String pdfSafe(String v){return v==null?"":v.replace('\n',' ').replace('\r',' ');}
    private String clipText(String v,int max){String s=pdfSafe(v);return s.length()<=max?s:s.substring(0,Math.max(1,max-1))+"…";}
    private String safeFileName(String v){String s=pdfSafe(v).replaceAll("[\\\\/:*?\"<>|]","_").trim();if(s.isEmpty())s="Smeta";return s.length()>45?s.substring(0,45):s;}
    private void message(String message){new AlertDialog.Builder(this).setMessage(message).setPositiveButton("Понятно",null).show();}
}
