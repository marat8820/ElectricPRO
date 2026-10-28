package com.electricpro.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.ContentValues;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

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

    private static class WorkLine {
        String name;
        double quantity;
        double price;

        WorkLine(String name, double quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        double total() {
            return quantity * price;
        }
    }

    private static class Order {
        String name;
        final List<WorkLine> lines = new ArrayList<>();

        Order(String name) {
            this.name = name;
        }

        double total() {
            double sum = 0;
            for (WorkLine line : lines) sum += line.total();
            return sum;
        }
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(navy);
        getWindow().setNavigationBarColor(navy);
        loadOrders();
        home();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (!onHome) {
            home();
        } else {
            super.onBackPressed();
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable bg(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        drawable.setStroke(dp(1), Color.rgb(28, 75, 113));
        return drawable;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout.LayoutParams lp(int width, int height) {
        return new LinearLayout.LayoutParams(
                width < 0 ? width : dp(width),
                height < 0 ? height : dp(height));
    }

    private void page(String title, String subtitle, boolean back) {
        onHome = !back;
        root = column();
        root.setPadding(dp(18), dp(18), dp(18), dp(24));
        root.setBackgroundColor(navy);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(navy);
        scroll.addView(root);
        setContentView(scroll);

        if (back) {
            TextView top = text("‹    " + title, 20, white, true);
            top.setPadding(0, dp(4), 0, dp(16));
            top.setOnClickListener(v -> home());
            root.addView(top, lp(-1, -2));
        } else {
            TextView logo = text("⚡", 42, yellow, true);
            logo.setGravity(Gravity.CENTER);
            root.addView(logo, lp(-1, 54));
            TextView brand = text("ЭЛЕКТРИК ", 27, white, true);
            brand.setGravity(Gravity.CENTER);
            TextView pro = text("PRO", 27, yellow, true);
            pro.setGravity(Gravity.CENTER);
            LinearLayout name = new LinearLayout(this);
            name.setGravity(Gravity.CENTER);
            name.addView(brand);
            name.addView(pro);
            root.addView(name, lp(-1, 38));
        }

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView sub = text(subtitle, 14, muted, false);
            sub.setGravity(back ? Gravity.START : Gravity.CENTER);
            sub.setPadding(0, dp(4), 0, dp(18));
            root.addView(sub, lp(-1, -2));
        }
    }

    private void addSpace(int height) {
        View space = new View(this);
        root.addView(space, lp(1, height));
    }

    private void actionButton(String label, Runnable action) {
        TextView button = text(label, 16, navy, true);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(12), dp(13), dp(12), dp(13));
        button.setBackground(bg(yellow, 12));
        LinearLayout.LayoutParams params = lp(-1, -2);
        params.topMargin = dp(8);
        params.bottomMargin = dp(5);
        root.addView(button, params);
        button.setOnClickListener(v -> action.run());
    }

    private void addGridCardRow(String icon1, String title1, String description1, Runnable action1,
                                String icon2, String title2, String description2, Runnable action2) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(0, dp(132), 1);
        leftParams.rightMargin = dp(6);
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(0, dp(132), 1);
        rightParams.leftMargin = dp(6);
        row.addView(makeGridCard(icon1, title1, description1, action1), leftParams);
        row.addView(makeGridCard(icon2, title2, description2, action2), rightParams);
        LinearLayout.LayoutParams rowParams = lp(-1, -2);
        rowParams.bottomMargin = dp(10);
        root.addView(row, rowParams);
    }

    private View makeGridCard(String icon, String title, String description, Runnable action) {
        LinearLayout card = column();
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(10), dp(10), dp(10), dp(10));
        card.setBackground(bg(panel, 14));
        TextView iconView = text(icon, 28, yellow, true);
        iconView.setGravity(Gravity.CENTER);
        card.addView(iconView, lp(-1, 38));
        TextView titleView = text(title, 15, white, true);
        titleView.setGravity(Gravity.CENTER);
        card.addView(titleView, lp(-1, -2));
        TextView descriptionView = text(description, 11, muted, false);
        descriptionView.setGravity(Gravity.CENTER);
        descriptionView.setPadding(0, dp(4), 0, 0);
        card.addView(descriptionView, lp(-1, -2));
        card.setOnClickListener(v -> action.run());
        return card;
    }

    private void infoCard(String title, String body) {
        LinearLayout card = column();
        card.setPadding(dp(15), dp(14), dp(15), dp(14));
        card.setBackground(bg(panel, 13));
        card.addView(text(title, 16, white, true));
        TextView bodyView = text(body, 14, muted, false);
        bodyView.setPadding(0, dp(7), 0, 0);
        card.addView(bodyView);
        LinearLayout.LayoutParams params = lp(-1, -2);
        params.bottomMargin = dp(10);
        root.addView(card, params);
    }

    private EditText field(String label, String initialValue) {
        TextView labelView = text(label, 13, muted, false);
        LinearLayout.LayoutParams labelParams = lp(-1, -2);
        labelParams.topMargin = dp(10);
        root.addView(labelView, labelParams);
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(initialValue);
        input.setTextColor(white);
        input.setHintTextColor(muted);
        input.setTextSize(16);
        input.setPadding(dp(12), dp(8), dp(12), dp(8));
        input.setBackground(bg(panel2, 9));
        input.setInputType(8194);
        root.addView(input, lp(-1, 48));
        return input;
    }

    private double value(EditText input) {
        try {
            return Double.parseDouble(input.getText().toString().trim().replace(',', '.'));
        } catch (Exception exception) {
            return Double.NaN;
        }
    }

    private boolean valid(double... values) {
        for (double value : values) {
            if (Double.isNaN(value) || Double.isInfinite(value)) return false;
        }
        return true;
    }

    private void resultCard(String title, String result, String note) {
        LinearLayout card = column();
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(bg(panel, 14));
        card.addView(text(title, 16, white, true));
        TextView resultView = text(result, 27, yellow, true);
        resultView.setPadding(0, dp(7), 0, dp(5));
        card.addView(resultView);
        if (note != null && !note.isEmpty()) card.addView(text(note, 13, muted, false));
        LinearLayout.LayoutParams params = lp(-1, -2);
        params.topMargin = dp(14);
        root.addView(card, params);
    }

    private void home() {
        page("", "Ваш помощник в электромонтажных работах", false);
        TextView headline = text("Знания. Расчёты.\nБезопасность.", 24, white, true);
        headline.setGravity(Gravity.CENTER);
        headline.setPadding(0, dp(12), 0, dp(20));
        root.addView(headline, lp(-1, -2));
        addGridCardRow("▦", "Калькулятор\nпроводов", "Ток и сечение", () -> calc(),
                "☼", "Освещение", "Световой поток", () -> light());
        addGridCardRow("⬟", "Безопасность", "Правила и защита", () -> safety(),
                "▤", "Справочник", "Формулы и таблицы", () -> reference());
        actionButton("▣  Мои заказы", () -> ordersPage());
        // Кнопка «Начать» удалена: разделы открываются через карточки меню.
        addSpace(8);
        TextView footer = text("Работай безопасно! Работай с умом!", 13, yellow, true);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer, lp(-1, -2));
    }

    private void calc() {
        page("Калькулятор проводов", "Ориентировочный расчёт электрической нагрузки", true);
        infoCard("Расчёт по току", "Для однофазной нагрузки: I ≈ P / U. Для более точного расчёта учитывайте коэффициент мощности и тип нагрузки.");
        EditText power = field("Мощность нагрузки (Вт)", "3500");
        EditText voltage = field("Напряжение (В)", "220");
        EditText cos = field("Коэффициент мощности cos φ (1 для простого расчёта)", "1");
        actionButton("▦  Рассчитать ток", () -> {
            double p = value(power), u = value(voltage), c = value(cos);
            if (!valid(p, u, c) || p <= 0 || u <= 0 || c <= 0 || c > 1) {
                message("Введите корректные числа. Мощность и напряжение должны быть больше нуля, cos φ — от 0 до 1.");
                return;
            }
            double current = p / (u * c);
            resultCard("Расчётный ток", String.format(Locale.getDefault(), "%.2f А", current),
                    "Это оценка нагрузки, а не готовый подбор кабеля или автомата.");
        });
        addSpace(12);
        infoCard("Падение напряжения", "Падение зависит от материала, сечения, длины линии, тока и схемы сети. Для выбора кабеля используйте действующие нормы и таблицы производителя.");
        EditText amps = field("Ток нагрузки (А)", "16");
        EditText length = field("Длина линии в одну сторону (м)", "20");
        EditText section = field("Сечение медного проводника (мм²)", "2.5");
        actionButton("Рассчитать падение напряжения", () -> {
            double current = value(amps), lineLength = value(length), area = value(section);
            if (!valid(current, lineLength, area) || current < 0 || lineLength <= 0 || area <= 0) {
                message("Введите корректные значения: ток не может быть отрицательным, длина и сечение должны быть больше нуля.");
                return;
            }
            double drop = 2 * 0.0175 * lineLength * current / area;
            double percent = drop / 220 * 100;
            resultCard("Оценочное падение напряжения", String.format(Locale.getDefault(), "%.2f В (%.1f%%)", drop, percent),
                    "Упрощённая оценка для медной однофазной линии при 220 В; не учитывает нагрев и реактивное сопротивление.");
        });
        infoCard("Важно", "Нельзя выбирать сечение только по этому калькулятору. Учитываются способ прокладки, материал, температура, группировка кабелей, длина, защита и местные нормы.");
    }

    private void light() {
        page("Освещение", "Быстрый ориентировочный расчёт", true);
        EditText area = field("Площадь помещения (м²)", "20");
        EditText lux = field("Требуемая освещённость (лк)", "300");
        EditText watts = field("Мощность одного светильника (Вт)", "12");
        EditText lumens = field("Световой поток одного светильника (лм)", "1200");
        actionButton("☼  Рассчитать освещение", () -> {
            double roomArea = value(area), illumination = value(lux), power = value(watts), flux = value(lumens);
            if (!valid(roomArea, illumination, power, flux) || roomArea <= 0 || illumination <= 0 || power <= 0 || flux <= 0) {
                message("Все значения должны быть больше нуля.");
                return;
            }
            double needed = roomArea * illumination;
            int count = (int) Math.ceil(needed / flux);
            resultCard("Ориентировочно светильников", String.valueOf(count),
                    String.format(Locale.getDefault(), "Суммарный поток: %.0f лм\nОриентировочная мощность: %.0f Вт", count * flux, count * power));
        });
        infoCard("Примечание", "Расчёт не учитывает коэффициент использования, высоту монтажа, загрязнение и отражение поверхностей. Для рабочего проекта требуется светотехнический расчёт.");
    }

    private void safety() {
        page("Безопасность", "Основные правила перед началом работ", true);
        infoCard("⚠  Отключите питание", "Отключите нужную цепь, исключите случайное повторное включение и обозначьте место работ.");
        infoCard("✓  Проверьте отсутствие напряжения", "Используйте исправный двухполюсный указатель и проверьте его работу до и после проверки цепи.");
        infoCard("⬟  Используйте СИЗ", "Подбирайте защитные очки, перчатки, инструмент и одежду под конкретную работу и оценённый риск.");
        infoCard("＋  Первая помощь", "При поражении током сначала безопасно отключите питание. Не касайтесь пострадавшего, пока источник опасности не устранён. Вызовите экстренную помощь.");
        infoCard("⛨  Защитные устройства", "Автоматы, УЗО, заземление и сечения проводников выбирают по проекту, условиям установки и действующим нормам.");
        infoCard("Важно", "Не выполняйте работы под напряжением без специальной квалификации, допуска и соответствующих процедур.");
    }

    private void reference() {
        page("Справочник", "Базовые формулы и справочные значения", true);
        infoCard("Закон Ома", "I = U / R\nТок (А) равен напряжению (В), делённому на сопротивление (Ом).");
        infoCard("Электрическая мощность", "P = U × I\nДля переменного тока и реальной нагрузки учитывайте cos φ: P ≈ U × I × cos φ.");
        infoCard("Сопротивление", "R = U / I\nСопротивление измеряется в омах (Ω).");
        infoCard("Падение напряжения", "Для простой однофазной медной линии ориентировочно: ΔU ≈ 2 × 0,0175 × L × I / S, где L — длина в одну сторону (м), I — ток (А), S — сечение (мм²).");
        infoCard("Сечение кабеля — не выбирайте только по току", "Допустимый ток зависит от способа прокладки, изоляции, температуры, количества нагруженных жил, группировки, материала и защитного аппарата. Используйте действующие таблицы ПУЭ/местных норм и данные производителя.");
        infoCard("Цвета проводников", "Защитный PE обычно жёлто-зелёный; нейтральный N — голубой. Цвета и назначение необходимо проверять по схеме и нормам конкретной установки, не полагаться только на цвет.");
    }


    // -------------------- МОИ ЗАКАЗЫ --------------------

    private void loadOrders() {
        orders.clear();
        String raw = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(ORDERS_KEY, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Order order = new Order(obj.optString("name", "Новый заказ"));
                JSONArray lines = obj.optJSONArray("lines");
                if (lines != null) {
                    for (int j = 0; j < lines.length(); j++) {
                        JSONObject line = lines.getJSONObject(j);
                        order.lines.add(new WorkLine(
                                line.optString("name", ""),
                                line.optDouble("quantity", 0),
                                line.optDouble("price", 0)));
                    }
                }
                orders.add(order);
            }
        } catch (Exception ignored) {
            orders.clear();
        }
    }

    private void saveOrders() {
        JSONArray array = new JSONArray();
        try {
            for (Order order : orders) {
                JSONObject obj = new JSONObject();
                obj.put("name", order.name);
                JSONArray lines = new JSONArray();
                for (WorkLine line : order.lines) {
                    JSONObject item = new JSONObject();
                    item.put("name", line.name);
                    item.put("quantity", line.quantity);
                    item.put("price", line.price);
                    lines.put(item);
                }
                obj.put("lines", lines);
                array.put(obj);
            }
        } catch (Exception ignored) {
            message("Не удалось сохранить заказы. Попробуйте ещё раз.");
            return;
        }
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putString(ORDERS_KEY, array.toString()).apply();
    }

    private String money(double amount) {
        return String.format(Locale.getDefault(), "%,.2f", amount).replace('\u00a0', ' ');
    }

    private String number(double amount) {
        if (Math.abs(amount - Math.rint(amount)) < 0.000001) {
            return String.format(Locale.getDefault(), "%.0f", amount);
        }
        return String.format(Locale.getDefault(), "%.2f", amount);
    }

    private void ordersPage() {
        page("Мои заказы", "Создавайте сметы на работы и редактируйте их в любое время", true);
        actionButton("＋  Создать заказ", () -> {
            Order order = new Order("Новый заказ");
            orderEditor(order, -1);
        });

        if (orders.isEmpty()) {
            addSpace(12);
            infoCard("Пока нет заказов", "Нажмите «Создать заказ». Добавляйте строки работ, указывайте количество или длину и цену за единицу. Сумма каждой строки и общий итог посчитаются автоматически.");
            return;
        }

        for (int i = 0; i < orders.size(); i++) {
            final int index = i;
            Order order = orders.get(i);
            LinearLayout card = column();
            card.setPadding(dp(14), dp(14), dp(14), dp(14));
            card.setBackground(bg(panel, 13));
            card.addView(text(order.name, 18, white, true));
            card.addView(text(order.lines.size() + " строк работ", 13, muted, false));
            TextView total = text("Итого: " + money(order.total()), 20, yellow, true);
            total.setPadding(0, dp(8), 0, dp(6));
            card.addView(total);
            LinearLayout buttons = new LinearLayout(this);
            buttons.setOrientation(LinearLayout.HORIZONTAL);
            TextView edit = smallButton("Открыть / изменить");
            TextView delete = smallButton("Удалить");
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, dp(44), 1);
            bp.rightMargin = dp(5);
            buttons.addView(edit, bp);
            LinearLayout.LayoutParams dp2 = new LinearLayout.LayoutParams(0, dp(44), 1);
            dp2.leftMargin = dp(5);
            buttons.addView(delete, dp2);
            edit.setOnClickListener(v -> orderEditor(orders.get(index), index));
            delete.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Удалить заказ?")
                    .setMessage("Заказ «" + orders.get(index).name + "» будет удалён.")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        orders.remove(index);
                        saveOrders();
                        ordersPage();
                    }).show());
            card.addView(buttons);
            LinearLayout.LayoutParams cp = lp(-1, -2);
            cp.bottomMargin = dp(10);
            root.addView(card, cp);
        }
    }

    private TextView smallButton(String label) {
        TextView button = text(label, 13, navy, true);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(5), dp(6), dp(5), dp(6));
        button.setBackground(bg(yellow, 9));
        return button;
    }

    private void orderEditor(Order order, int existingIndex) {
        page("Заказ", "Добавьте работы — стоимость считается автоматически", true);
        EditText orderName = field("Название заказа / объект", order.name);
        actionButton("＋  Добавить строку работ", () -> lineDialog(order, -1, orderName.getText().toString()));
        TextView totalView = text("", 22, yellow, true);
        totalView.setPadding(0, dp(12), 0, dp(10));
        root.addView(totalView, lp(-1, -2));

        LinearLayout linesContainer = column();
        root.addView(linesContainer, lp(-1, -2));

        Runnable refresh = () -> {
            linesContainer.removeAllViews();
            if (order.lines.isEmpty()) {
                TextView empty = text("Строк работ пока нет. Нажмите «Добавить строку работ».", 14, muted, false);
                empty.setPadding(0, dp(8), 0, dp(8));
                linesContainer.addView(empty);
            }
            for (int i = 0; i < order.lines.size(); i++) {
                final int lineIndex = i;
                WorkLine line = order.lines.get(i);
                LinearLayout card = column();
                card.setPadding(dp(12), dp(12), dp(12), dp(12));
                card.setBackground(bg(panel, 11));
                card.addView(text(line.name, 16, white, true));
                card.addView(text("Количество / длина: " + number(line.quantity), 13, muted, false));
                card.addView(text("Цена за единицу: " + money(line.price), 13, muted, false));
                card.addView(text("Сумма за работу: " + money(line.total()), 17, yellow, true));
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                TextView edit = smallButton("Изменить");
                TextView delete = smallButton("Удалить строку");
                LinearLayout.LayoutParams e = new LinearLayout.LayoutParams(0, dp(40), 1);
                e.rightMargin = dp(5);
                row.addView(edit, e);
                LinearLayout.LayoutParams d = new LinearLayout.LayoutParams(0, dp(40), 1);
                d.leftMargin = dp(5);
                row.addView(delete, d);
                edit.setOnClickListener(v -> lineDialog(order, lineIndex, orderName.getText().toString()));
                delete.setOnClickListener(v -> new AlertDialog.Builder(this)
                        .setTitle("Удалить строку?")
                        .setMessage(line.name)
                        .setNegativeButton("Отмена", null)
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            order.lines.remove(lineIndex);
                            refresh.run();
                        }).show());
                card.addView(row);
                LinearLayout.LayoutParams p = lp(-1, -2);
                p.bottomMargin = dp(8);
                linesContainer.addView(card, p);
            }
            totalView.setText("ИТОГО ЗА ЗАКАЗ: " + money(order.total()));
        };
        refresh.run();

        actionButton("▤  Создать PDF сметы", () -> {
            String name = orderName.getText().toString().trim();
            if (name.isEmpty()) {
                message("Сначала введите название заказа.");
                return;
            }
            if (order.lines.isEmpty()) {
                message("Добавьте хотя бы одну строку работ перед созданием PDF.");
                return;
            }
            order.name = name;
            createEstimatePdf(order);
        });

        actionButton("Сохранить заказ", () -> {
            String name = orderName.getText().toString().trim();
            if (name.isEmpty()) {
                message("Введите название заказа или объекта.");
                return;
            }
            if (order.lines.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle("В заказе нет работ")
                        .setMessage("Сохранить заказ без строк работ?")
                        .setNegativeButton("Вернуться", null)
                        .setPositiveButton("Сохранить", (dialog, which) -> {
                            order.name = name;
                            persistOrder(order, existingIndex);
                        }).show();
                return;
            }
            order.name = name;
            persistOrder(order, existingIndex);
        });
    }

    private void persistOrder(Order order, int existingIndex) {
        order.name = order.name.trim();
        if (existingIndex >= 0 && existingIndex < orders.size()) {
            orders.set(existingIndex, order);
        } else {
            orders.add(order);
        }
        saveOrders();
        message("Заказ «" + order.name + "» сохранён.\nИтого: " + money(order.total()));
        ordersPage();
    }

    private void lineDialog(Order order, int lineIndex, String currentOrderName) {
        WorkLine old = lineIndex >= 0 && lineIndex < order.lines.size()
                ? order.lines.get(lineIndex) : null;
        LinearLayout layout = column();
        layout.setPadding(dp(18), dp(4), dp(18), dp(4));

        EditText name = dialogField("Название работ", old == null ? "" : old.name);
        EditText quantity = dialogField("Количество или длина", old == null ? "1" : number(old.quantity));
        EditText price = dialogField("Цена работы за единицу", old == null ? "0" : number(old.price));
        TextView calculated = text("Сумма за работу: 0.00", 18, yellow, true);
        calculated.setPadding(0, dp(10), 0, dp(4));
        layout.addView(text("Название работ", 13, muted, false));
        layout.addView(name, lp(-1, 48));
        layout.addView(text("Количество или длина", 13, muted, false));
        layout.addView(quantity, lp(-1, 48));
        layout.addView(text("Цена работы за единицу", 13, muted, false));
        layout.addView(price, lp(-1, 48));
        layout.addView(calculated);

        Runnable update = () -> {
            double q = parseDialogNumber(quantity.getText().toString());
            double p = parseDialogNumber(price.getText().toString());
            if (!Double.isNaN(q) && !Double.isInfinite(q) && !Double.isNaN(p) && !Double.isInfinite(p) && q >= 0 && p >= 0) {
                calculated.setText("Сумма за работу: " + money(q * p));
            } else {
                calculated.setText("Сумма за работу: —");
            }
        };
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { update.run(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        quantity.addTextChangedListener(watcher);
        price.addTextChangedListener(watcher);
        update.run();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(old == null ? "Новая строка работ" : "Изменить строку")
                .setView(layout)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить строку", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String workName = name.getText().toString().trim();
            double q = parseDialogNumber(quantity.getText().toString());
            double p = parseDialogNumber(price.getText().toString());
            if (workName.isEmpty()) {
                message("Введите название работ.");
                return;
            }
            if (Double.isNaN(q) || Double.isInfinite(q) || Double.isNaN(p) || Double.isInfinite(p) || q <= 0 || p < 0) {
                message("Количество или длина должны быть больше нуля, цена не может быть отрицательной.");
                return;
            }
            WorkLine line = new WorkLine(workName, q, p);
            if (lineIndex >= 0 && lineIndex < order.lines.size()) order.lines.set(lineIndex, line);
            else order.lines.add(line);
            dialog.dismiss();
            orderEditor(order, findOrderIndex(order));
        }));
        dialog.show();
    }

    private int findOrderIndex(Order order) {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i) == order) return i;
        }
        return -1;
    }

    private EditText dialogField(String label, String initialValue) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(initialValue);
        input.setHint(label);
        input.setTextColor(white);
        input.setHintTextColor(muted);
        input.setTextSize(16);
        input.setPadding(dp(12), dp(8), dp(12), dp(8));
        input.setBackground(bg(panel2, 9));
        input.setInputType(8194);
        LinearLayout.LayoutParams p = lp(-1, 48);
        p.topMargin = dp(5);
        p.bottomMargin = dp(5);
        layoutLabelHint(input, label);
        return input;
    }

    private void layoutLabelHint(EditText input, String label) {
        input.setHint(label);
    }

    private double parseDialogNumber(String raw) {
        try {
            if (raw == null || raw.trim().isEmpty()) return Double.NaN;
            return Double.parseDouble(raw.trim().replace(',', '.'));
        } catch (Exception ignored) {
            return Double.NaN;
        }
    }


    private void createEstimatePdf(Order order) {
        PdfDocument document = new PdfDocument();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final int pageWidth = 595;
        final int pageHeight = 842;
        final int left = 42;
        final int right = 553;
        final int rowHeight = 42;
        int pageNumber = 0;
        PdfDocument.Page page = null;
        Canvas canvas = null;
        int y = 0;

        try {
            for (int i = 0; i < order.lines.size(); i++) {
                if (page == null || y > pageHeight - 100) {
                    if (page != null) document.finishPage(page);
                    pageNumber++;
                    PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(
                            pageWidth, pageHeight, pageNumber).create();
                    page = document.startPage(info);
                    canvas = page.getCanvas();
                    y = 48;

                    paint.setColor(Color.rgb(4, 20, 39));
                    paint.setTextSize(22);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    canvas.drawText("ЭЛЕКТРИК PRO", left, y, paint);
                    y += 28;

                    paint.setColor(Color.rgb(30, 60, 90));
                    paint.setTextSize(17);
                    canvas.drawText("СМЕТА НА РАБОТЫ", left, y, paint);
                    y += 28;

                    paint.setColor(Color.BLACK);
                    paint.setTextSize(12);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    canvas.drawText("Заказ: " + pdfSafe(order.name), left, y, paint);
                    y += 18;
                    canvas.drawText("Дата: " + new java.text.SimpleDateFormat(
                            "dd.MM.yyyy", Locale.getDefault()).format(new java.util.Date()), left, y, paint);
                    y += 25;

                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    paint.setTextSize(10);
                    canvas.drawText("№", left, y, paint);
                    canvas.drawText("Наименование работ", left + 28, y, paint);
                    canvas.drawText("Кол-во", 335, y, paint);
                    canvas.drawText("Цена", 405, y, paint);
                    canvas.drawText("Сумма", 475, y, paint);
                    y += 8;
                    paint.setStrokeWidth(1);
                    canvas.drawLine(left, y, right, y, paint);
                    y += 20;
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                }

                WorkLine line = order.lines.get(i);
                paint.setTextSize(10);
                paint.setColor(Color.BLACK);
                canvas.drawText(String.valueOf(i + 1), left, y, paint);
                canvas.drawText(clipText(line.name, 36), left + 28, y, paint);
                canvas.drawText(number(line.quantity), 335, y, paint);
                canvas.drawText(money(line.price), 405, y, paint);
                canvas.drawText(money(line.total()), 475, y, paint);
                y += rowHeight;
                paint.setColor(Color.LTGRAY);
                canvas.drawLine(left, y - 17, right, y - 17, paint);
                paint.setColor(Color.BLACK);
            }

            if (page == null) {
                PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(
                        pageWidth, pageHeight, 1).create();
                page = document.startPage(info);
                canvas = page.getCanvas();
                y = 48;
                paint.setColor(Color.rgb(4, 20, 39));
                paint.setTextSize(22);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                canvas.drawText("ЭЛЕКТРИК PRO", left, y, paint);
                y += 35;
            }

            if (y > pageHeight - 75) {
                document.finishPage(page);
                pageNumber++;
                PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(
                        pageWidth, pageHeight, pageNumber).create();
                page = document.startPage(info);
                canvas = page.getCanvas();
                y = 55;
            }

            y += 4;
            paint.setColor(Color.BLACK);
            paint.setTextSize(14);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("ИТОГО: " + money(order.total()), left, y, paint);
            y += 35;
            paint.setTextSize(9);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            canvas.drawText("Документ сформирован приложением Электрик PRO", left, y, paint);
            document.finishPage(page);

            String fileName = safeFileName(order.name) + "_" +
                    new java.text.SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
                            .format(new java.util.Date()) + ".pdf";
            OutputStream output = null;
            Uri savedUri = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + "/ElectricPRO");
                savedUri = getContentResolver().insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (savedUri == null) throw new java.io.IOException("Не удалось создать PDF-файл");
                output = getContentResolver().openOutputStream(savedUri);
            } else {
                File directory = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                if (directory == null) throw new java.io.IOException("Папка документов недоступна");
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new java.io.IOException("Не удалось создать папку документов");
                }
                File file = new File(directory, fileName);
                output = new FileOutputStream(file);
            }

            if (output == null) throw new java.io.IOException("Не удалось открыть PDF-файл");
            document.writeTo(output);
            output.close();
            message("PDF-смета создана: " + fileName +
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                            ? "\nПапка: Загрузки/ElectricPRO" : "\nПапка документов приложения"));
        } catch (Exception e) {
            message("Не удалось создать PDF-смету: " + e.getMessage());
        } finally {
            document.close();
        }
    }

    private String pdfSafe(String value) {
        if (value == null) return "";
        return value.replace('\n', ' ').replace('\r', ' ');
    }

    private String clipText(String value, int max) {
        String safe = pdfSafe(value);
        if (safe.length() <= max) return safe;
        return safe.substring(0, Math.max(1, max - 1)) + "…";
    }

    private String safeFileName(String value) {
        String safe = pdfSafe(value).replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        if (safe.isEmpty()) safe = "Smeta";
        return safe.length() > 45 ? safe.substring(0, 45) : safe;
    }

    private void message(String message) {
        new AlertDialog.Builder(this).setMessage(message).setPositiveButton("Понятно", null).show();
    }
}
