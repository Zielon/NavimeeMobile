package org.pl.android.drively.ui.regulations;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.barteksc.pdfviewer.PDFView;

import org.pl.android.drively.R;

public class RegulationsFragment extends Fragment {

    public RegulationsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PDFView pdf = (PDFView) getView().findViewById(R.id.regulation_pdf);
        //pdf.fromUri(Uri.parse())

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_regulations, container, false);
    }
}
