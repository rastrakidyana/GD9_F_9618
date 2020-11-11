package com.gd9_x_yyyy.Views;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gd9_x_yyyy.API.MahasiswaAPI;
import com.gd9_x_yyyy.MainActivity;
import com.gd9_x_yyyy.Models.Mahasiswa;
import com.gd9_x_yyyy.R;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.android.volley.Request.Method.POST;
import static com.android.volley.Request.Method.PUT;


public class TambahEdit extends Fragment {
    private TextInputEditText txtNpm, txtNama;
    private ImageView ivGambar;
    private Button btnSimpan, btnBatal;
    private String status, selectedJenisKelamin, selectedProdi;
    private Mahasiswa mahasiswa;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tambah_edit, container, false);
        setAtribut(view);

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtNama.getText().length()<1 || txtNpm.getText().length() != 9)
                {
                    if(txtNama.getText().length()<1)
                        txtNama.setError("Data Tidak Boleh Kosong");
                    if(txtNpm.getText().length() != 9)
                        txtNpm.setError("Npm harus 9 angka");
                }
                else
                {
                    String npm      = txtNpm.getText().toString();
                    String nama     = txtNama.getText().toString();

                    if(status.equals("tambah"))
                        tambahMahasiswa(npm, nama, selectedJenisKelamin, selectedProdi);
                    else
                        editMahasiswa(npm, nama, selectedJenisKelamin, selectedProdi);
                }
            }
        });

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new ViewsMahasiswa());
            }
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.btnSearch).setVisible(false);
        menu.findItem(R.id.btnAdd).setVisible(false);
    }

    public void setAtribut(View view){
        mahasiswa   = (Mahasiswa) getArguments().getSerializable("mahasiswa");
        txtNpm      = view.findViewById(R.id.txtNpm);
        txtNama     = view.findViewById(R.id.txtNama);
        btnSimpan   = view.findViewById(R.id.btnSimpan);
        btnBatal    = view.findViewById(R.id.btnBatal);
        ivGambar    = view.findViewById(R.id.ivGambar);

        status = getArguments().getString("status");
        final String[] JKArray = getResources().getStringArray(R.array.jenisKelamin);
        final String[] ProdiArray = getResources().getStringArray(R.array.prodi);

        if(status.equals("tambah"))
        {
            selectedJenisKelamin = JKArray[0];
            selectedProdi = ProdiArray[0];
            Glide.with(getContext())
                    .load("https://1080motion.com/wp-content/uploads/2018/06/NoImageFound.jpg.png")
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .circleCrop()
                    .skipMemoryCache(true)
                    .into(ivGambar);
        }
        else
        {
            txtNpm.setEnabled(false);
            txtNama.setText(mahasiswa.getNama());
            txtNpm.setText(mahasiswa.getNpm());
            for(String jk : JKArray)
            {
                if(jk.equals(mahasiswa.getJenis_kelamin()))
                    selectedJenisKelamin = jk;
            }
            for(String prodi : ProdiArray)
            {
                if(prodi.equals(mahasiswa.getProdi()))
                    selectedProdi = prodi;
            }
            Glide.with(getContext())
                    .load(mahasiswa.getGambar())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(ivGambar);
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.jenisKelamin, android.R.layout.simple_spinner_item);
        final AutoCompleteTextView jenisKelaminDropdown = view.findViewById(R.id.txtJenisKelamin);
        jenisKelaminDropdown.setText(selectedJenisKelamin);
        jenisKelaminDropdown.setAdapter(adapter);
        jenisKelaminDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedJenisKelamin = jenisKelaminDropdown.getEditableText().toString();
            }
        });

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getContext(),
                R.array.prodi, android.R.layout.simple_spinner_item);
        final AutoCompleteTextView prodiDropdown = view.findViewById(R.id.txtProdi);
        prodiDropdown.setText(selectedProdi);
        prodiDropdown.setAdapter(adapter2);
        prodiDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedProdi = prodiDropdown.getEditableText().toString();
            }
        });
    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (Build.VERSION.SDK_INT >= 26) {
            fragmentTransaction.setReorderingAllowed(false);
        }
        fragmentTransaction.replace(R.id.tambah_edit_fragment, fragment)
                .detach(this)
                .attach(this)
                .commit();
    }

    public void tambahMahasiswa(final String npm, final String nama, final String jk, final String prodi){
        RequestQueue queue = Volley.newRequestQueue(getContext());

        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("loading...");
        progressDialog.setTitle("Menambahkan data mahasiswa");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(POST, MahasiswaAPI.URL_ADD, new
                Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getString("status").equals("Success")){
                                loadFragment(new ViewsMahasiswa());
                            }
                            Toast.makeText(getContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("npm", npm);
                        params.put("nama", nama);
                        params.put("prodi", prodi);
                        params.put("jenis_kelamin", jk);

                        return params;
                    }
                };
                queue.add(stringRequest);
    }

    public void editMahasiswa(final String npm, final String nama, final String jk, final String prodi){
        RequestQueue queue = Volley.newRequestQueue(getContext());

        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("loading...");
        progressDialog.setTitle("Mengubah data mahasiswa");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(PUT, MahasiswaAPI.URL_UPDATE + npm, new
                Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject obj = new JSONObject(response);
                            Toast.makeText(getContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                            loadFragment(new ViewsMahasiswa());
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("nama", nama);
                params.put("prodi", prodi);
                params.put("jenis_kelamin", jk);

                return params;
            }
        };
        queue.add(stringRequest);
    }
}
