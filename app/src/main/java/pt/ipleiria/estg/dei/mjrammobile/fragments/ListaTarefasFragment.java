package pt.ipleiria.estg.dei.mjrammobile.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import pt.ipleiria.estg.dei.mjrammobile.R;
import pt.ipleiria.estg.dei.mjrammobile.adaptadores.ListaTarefasAdaptador;
import pt.ipleiria.estg.dei.mjrammobile.listeners.TarefasListener;
import pt.ipleiria.estg.dei.mjrammobile.modelo.Singleton;
import pt.ipleiria.estg.dei.mjrammobile.modelo.Tarefa;
import pt.ipleiria.estg.dei.mjrammobile.utils.JsonParser;

public class ListaTarefasFragment extends Fragment implements TarefasListener {

    private int id_voo;
    private View v;
    private ListView listview;
    ArrayList<Tarefa> tarefas;
    private TextInputEditText ET_search;
    private TextView tv_titulo;
    private TextInputLayout til_searchFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        id_voo = arguments.getInt("ID_VOO");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_lista_tarefas, container, false);

        //Envia para a listview tudo
        listview = v.findViewById(R.id.lvTarefas);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (!JsonParser.isConnectionInternet(getContext())){
                    Toast.makeText(getContext(), "Sem ligação à internet", Toast.LENGTH_LONG).show();
                }else {
                    DetalhesTarefaFragment fragment = new DetalhesTarefaFragment();
                    Bundle arguments = new Bundle();
                    arguments.putInt("ID_VOO", id_voo);
                    arguments.putInt("ID_TAREFA", (int) id);
                    fragment.setArguments(arguments);

                    FragmentTransaction fr = getFragmentManager().beginTransaction();
                    fr.replace(R.id.Fl_menu, fragment);
                    fr.commit();
                }

            }});
        ET_search = v.findViewById(R.id.ET_search);
        //Listener para o enviar para outro fragment
        FloatingActionButton FB_Add_tarefa = (FloatingActionButton) v.findViewById(R.id.FB_Add_tarefas);
        FB_Add_tarefa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!JsonParser.isConnectionInternet(getContext())){
                    Toast.makeText(getContext(), "Sem ligação à internet", Toast.LENGTH_LONG).show();
                }else {
                    AdicionarTarefaFragment fragment = new AdicionarTarefaFragment();
                    Bundle arguments = new Bundle();
                    arguments.putInt("ID_VOO", id_voo);
                    fragment.setArguments(arguments);

                    FragmentTransaction fr = getFragmentManager().beginTransaction();
                    fr.replace(R.id.Fl_menu, fragment);
                    fr.commit();
                }
            }
        });

        //Verifica se esta o search ou não
        ImageButton ib_search_tarefa = (ImageButton) v.findViewById(R.id.ib_search_tarefas);
        ib_search_tarefa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                til_searchFilter = (TextInputLayout) v.findViewById(R.id.searchFilter);
                tv_titulo = (TextView) v.findViewById(R.id.tv_titulo_tarefas);
                if(tv_titulo.getVisibility() == v.VISIBLE){
                    tv_titulo.setVisibility(tv_titulo.GONE);
                    til_searchFilter.setVisibility(til_searchFilter.VISIBLE);
                    til_searchFilter.setEnabled(true);
                }else{
                    tv_titulo.setVisibility(tv_titulo.VISIBLE);
                    til_searchFilter.setVisibility(til_searchFilter.GONE);
                    til_searchFilter.setEnabled(false);
                }
            }
        });

        Singleton.getInstance(getContext()).setTarefasListener(this);
        Singleton.getInstance(getContext()).getAllTarefasAPI(id_voo, getContext());

        return v;
    }

    @Override
    public void onRefreshListaTarefas(ArrayList<Tarefa> listaTarefas) {

        if(listaTarefas != null){
            listview.setAdapter(new ListaTarefasAdaptador(getContext(), listaTarefas));
        }
    }
}