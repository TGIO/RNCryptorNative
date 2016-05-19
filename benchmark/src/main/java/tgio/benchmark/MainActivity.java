package tgio.benchmark;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.cryptonode.jncryptor.AES256JNCryptor;
import org.cryptonode.jncryptor.CryptorException;
import org.cryptonode.jncryptor.JNCryptor;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

import tgio.rncryptor.RNCryptorNative;

public class MainActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    public static final int INTSTRINGS_COUNT = 25;
    protected String[] cases = new String[]{
            "encrypt " + INTSTRINGS_COUNT, "decrypt " + INTSTRINGS_COUNT
    };
    String password = "secretsquirrel";
    BarEntry v1 = new BarEntry(new float[]{0, 0}, 0);
    BarEntry v2 = new BarEntry(new float[]{0, 0}, 1);
    Handler handler;
    String[] rawStringsToEncrypt = new String[INTSTRINGS_COUNT];
    String[] encriptedWithJNCryptor = new String[INTSTRINGS_COUNT];
    String[] encriptedWithRNCryptorNative = new String[INTSTRINGS_COUNT];
    private BarChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        mChart = (BarChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        mChart.setDescription("");

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);
        mChart.setNoDataText("Running benchmarks. please wait aprox 20 seconds.");
        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
        // change the position of the y-labels
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setValueFormatter(new MyYAxisValueFormatter());
        leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)
        mChart.getAxisRight().setEnabled(false);

        XAxis xLabels = mChart.getXAxis();
        xLabels.setPosition(XAxisPosition.TOP);

        // mChart.setDrawXLabels(false);
        // mChart.setDrawYLabels(false);

        Legend l = mChart.getLegend();
        l.setPosition(LegendPosition.BELOW_CHART_RIGHT);
        l.setFormSize(14f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);
        l.setTextSize(18);



        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                setupStrings();

                benchEncrypt();

                benchDecrypt();
            }
        });
    }

    void setupStrings() {
        RandomString rnstr = new RandomString(200);
        for (int i = 0; i < INTSTRINGS_COUNT; i++) {
            rawStringsToEncrypt[i] = rnstr.nextString();
        }
    }

    void benchEncrypt() {
        long nativeEncript = runBenchEncrypt(true, rawStringsToEncrypt);
        long encript = runBenchEncrypt(false, rawStringsToEncrypt);
        update(v1, nativeEncript, encript);
    }

    void benchDecrypt() {
        update(v2, runBenchDecrypt(true), runBenchDecrypt(false));
        printData();
    }


    long runBenchEncrypt(boolean isNative, String[] inputs) {
        System.out.println("MainActivity.runBenchEncrypt");
        long startTime = System.currentTimeMillis();
        if (isNative) {
            encryptWithRNCryptorNative(inputs);
        } else {
            try {
                encryptWithJNCryptor(inputs);
            } catch (CryptorException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        long stopTime = System.currentTimeMillis();
        return stopTime - startTime;
    }

    void update(BarEntry entry, float val1, float val2) {
        System.out.println("entry = [" + entry + "], val1 = [" + val1 + "], val2 = [" + val2 + "]");
        entry.setVals(new float[]{val1, val2});

    }

    long runBenchDecrypt(boolean isNative) {
        System.out.println("MainActivity.runBenchDecrypt");
        long startTime = System.currentTimeMillis();
        if (isNative) {
            decryptWithRNCryptorNative();
        } else {
            try {
                decryptWithJNCryptor();
            } catch (CryptorException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        long stopTime = System.currentTimeMillis();
        return stopTime - startTime;
    }

    void printData() {
        ArrayList<String> xVals = new ArrayList<>();
        Collections.addAll(xVals, cases);

        ArrayList<BarEntry> yVals1 = new ArrayList<>();

        yVals1.add(v1);
        yVals1.add(v2);

        BarDataSet set1 = new BarDataSet(yVals1, "");
        set1.setColors(getColors());
        set1.setStackLabels(new String[]{"rncryptor-native", "jncryptor"});
        set1.setValueTextColor(Color.BLACK);
        set1.setValueTextSize(18);
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        final BarData data = new BarData(xVals, dataSets);
        data.setValueFormatter(new MyValueFormatter());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mChart.setData(data);

                mChart.invalidate();
            }
        });

    }

    void encryptWithRNCryptorNative(String[] inputs) {
        RNCryptorNative rncryptor = new RNCryptorNative();
        for (int i = 0; i < inputs.length; i++) {
            encriptedWithRNCryptorNative[i] = new String(rncryptor.encrypt(inputs[i], password));
        }
    }

    void encryptWithJNCryptor(String[] inputs) throws CryptorException, UnsupportedEncodingException {
        JNCryptor cryptor = new AES256JNCryptor();
        for (int i = 0; i < inputs.length; i++) {
            byte[] expB = inputs[i].getBytes("UTF-8");
            byte[] bytes = cryptor.encryptData(expB, password.toCharArray());
            String result = new String(Base64.encode(bytes, Base64.NO_WRAP), "UTF-8");
            System.out.println(result);
            encriptedWithJNCryptor[i] = result;
        }
    }

    void decryptWithRNCryptorNative() {
        RNCryptorNative rncryptor = new RNCryptorNative();
        for (int i = 0; i < encriptedWithRNCryptorNative.length; i++) {
            String result = rncryptor.decrypt(encriptedWithRNCryptorNative[i], password);
        }
    }

    void decryptWithJNCryptor() throws CryptorException, UnsupportedEncodingException {
        JNCryptor cryptor = new AES256JNCryptor();
        for (int i = 0; i < encriptedWithJNCryptor.length; i++) {
            byte[] b = Base64.decode(encriptedWithJNCryptor[i], Base64.NO_WRAP);
            String decrypted = new String(cryptor.decryptData(b, password.toCharArray()));
            System.out.println(decrypted);
        }
    }

    private int[] getColors() {

        int stacksize = 2;

        // have as many colors as stack-values per entry
        int[] colors = new int[stacksize];

        System.arraycopy(ColorTemplate.COLORFUL_COLORS, 0, colors, 0, stacksize);

        return colors;
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

        BarEntry entry = (BarEntry) e;

        if (entry.getVals() != null)
            Log.i("VAL SELECTED", "Value: " + entry.getVals()[h.getStackIndex()]);
        else
            Log.i("VAL SELECTED", "Value: " + entry.getVal());
    }

    @Override
    public void onNothingSelected() {
        // TODO Auto-generated method stub

    }


}
