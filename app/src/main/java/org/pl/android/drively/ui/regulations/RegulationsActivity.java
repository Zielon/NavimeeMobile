package org.pl.android.drively.ui.regulations;

import android.os.Bundle;
import android.app.Activity;
import android.view.MenuItem;

import com.github.barteksc.pdfviewer.PDFView;

import org.pl.android.drively.R;

import java.io.IOException;

public class RegulationsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regulations);

        PDFView pdf = (PDFView) findViewById(R.id.regulation_pdf);

        pdf.fromAsset("regulation.pdf")
                .enableSwipe(true)
                .defaultPage(0)
                .enableDoubletap(true)
                .swipeHorizontal(false)
                .enableAntialiasing(true)
                .spacing(0)
                .load();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
