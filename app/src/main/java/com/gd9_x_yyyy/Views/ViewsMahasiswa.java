package com.gd9_x_yyyy.Views;

import android.app.ProgressDialog;
import android.content.Context;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gd9_x_yyyy.API.MahasiswaAPI;
import com.gd9_x_yyyy.Adapters.AdapterMahasiswa;
import com.gd9_x_yyyy.Models.Mahasiswa;
import com.gd9_x_yyyy.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.android.volley.Request.Method.GET;

public class ViewsMahasiswa extends Fragment {
    private RecyclerView recyclerView;
    private AdapterMahasiswa adapter;
    private List<Mahasiswa> listMahasiswa;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_views_mahasiswa, container, false);
        setAdapter();
        getMahasiswa();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_bar, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem         = menu.findItem(R.id.btnSearch);
        final MenuItem addItem      = menu.findItem(R.id.btnAdd);

        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                addItem.setVisible(false);
                return false;
            }
        });

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.btnAdd) {
            Bundle data = new Bundle();
            data.putString("status", "tambah");
            TambahEdit tambahEdit = new TambahEdit();
            tambahEdit.setArguments(data);

            loadFragment(tambahEdit);
        }
        return super.onOptionsItemSelected(item);
    }

    public void setAdapter(){
        getActivity().setTitle("Data Mahasiswa");
        listMahasiswa = new ArrayList<Mahasiswa>();
        recyclerView = view.findViewById(R.id.recycler_view);
        adapter = new AdapterMahasiswa(view.getContext(), listMahasiswa);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.views_mahasiswa_fragment,fragment)
                .commit();
    }

    //Fungsi menampilkan data mahasiswa
    public void getMahasiswa() {
        RequestQueue queue = Volley.newRequestQueue(view.getContext());

        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(view.getContext());
        progressDialog.setMessage("loading....");
        progressDialog.setTitle("Menampilkan data mahasiswa");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        final JsonObjectRequest stringRequest = new JsonObjectRequest(GET, MahasiswaAPI.URL_SELECT
                , null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    JSONArray jsonArray = response.getJSONArray("mahasiswa");

                    if (!listMahasiswa.isEmpty())
                        listMahasiswa.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                        String npm = jsonObject.optString("npm");
                        String nama = jsonObject.optString("nama");
                        String jenis_kelamin = jsonObject.optString("jenis_kelamin");
                        String prodi = jsonObject.optString("prodi");
                        String gambar = jsonObject.optString("gambar");

                        Mahasiswa mahasiswa =
                                new Mahasiswa(npm, nama, jenis_kelamin, prodi, gambar);

                        listMahasiswa.add(mahasiswa);
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast.makeText(view.getContext(), response.optString("message"),
                        Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(view.getContext(), error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
    }
}
