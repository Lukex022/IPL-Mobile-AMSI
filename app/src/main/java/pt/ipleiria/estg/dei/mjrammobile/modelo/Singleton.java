package pt.ipleiria.estg.dei.mjrammobile.modelo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pt.ipleiria.estg.dei.mjrammobile.MainActivity;
import pt.ipleiria.estg.dei.mjrammobile.listeners.AviaoListener;
import pt.ipleiria.estg.dei.mjrammobile.listeners.HangarListener;
import pt.ipleiria.estg.dei.mjrammobile.listeners.LoginListener;
import pt.ipleiria.estg.dei.mjrammobile.listeners.RecursoListener;
import pt.ipleiria.estg.dei.mjrammobile.listeners.TarefaSingleListener;
import pt.ipleiria.estg.dei.mjrammobile.listeners.TarefasListener;
import pt.ipleiria.estg.dei.mjrammobile.listeners.PerfilListener;
import pt.ipleiria.estg.dei.mjrammobile.listeners.VoosListener;
import pt.ipleiria.estg.dei.mjrammobile.utils.JsonParser;


public class Singleton {

    private static  Singleton instance = null;
    private static RequestQueue volleyQueue = null;
    private String token;
    private static String mUrlAPIVoos ="http://10.0.2.2:80/WEB-PSI-SIS/mjram/backend/web/api/voo/allvoo?access-token=";
    private static final String mUrlAPILogin = "http://10.0.2.2:80/WEB-PSI-SIS/mjram/backend/web/api/auth/login";
    private static final String mUrlAPIBase = "http://10.0.2.2:80/WEB-PSI-SIS/mjram/backend/web/api/";
    private static final String mUrlAPIAccessToken = "?access-token=";
    //private static final String mUrlAPILogin = "http://amsi.dei.estg.ipleiria.pt/api/auth/login";
    private LoginListener loginListener;
    private VoosListener voosListener;
    private TarefasListener tarefasListener;
    private AviaoListener aviaoListener;
    private RecursoListener recursosListener;
    private HangarListener hangarListener;
    private PerfilListener perfilListener;
    private TarefaSingleListener tarefaSingleListener;

    private ArrayList<Voo> voos;
    private MyBDHelper myBDHelper;
    private ArrayList<Tarefa> tarefas;
    //private ArrayList<Ocupacao> ocupacoes;
    private ArrayList<Recurso> recursos;
    private ArrayList<Hangar> hangares;
    private Perfil perfil;
    private Aviao aviao;
    private TarefaSingle tarefaSingle;

    public static synchronized Singleton getInstance(Context context){
        if(instance == null)
        {
            instance = new Singleton(context);
            volleyQueue = Volley.newRequestQueue(context);
        }
        return instance;
    }

    public void setLoginListener(LoginListener loginListener) {
        this.loginListener = loginListener;
    }
    public void setTarefasListener(TarefasListener tarefasListener) {
        this.tarefasListener = tarefasListener;
    }

    public void setTarefaSingleListener(TarefaSingleListener tarefaSingleListener) {
        this.tarefaSingleListener = tarefaSingleListener;
    }

    public void setPerfilListener(PerfilListener perfilListener){
        this.perfilListener = perfilListener;
    }

    public void setAviaoListener(AviaoListener aviaoListener) {
        this.aviaoListener = aviaoListener;
    }

    public void setVoosListener(VoosListener voosListener) { // para informar a vista
        this.voosListener = voosListener;
    }

    public void setRecursosListener(RecursoListener recursosListener) { // para informar a vista
        this.recursosListener = recursosListener;
    }

    public void setHangaresListener(HangarListener hangaresListener) { // para informar a vista
        this.hangarListener = hangaresListener;
    }

    private Singleton(Context context){

        voos = new ArrayList<>();
        myBDHelper = new MyBDHelper(context);
        //aviaos = new ArrayList<>();
        tarefas = new ArrayList<>();
        //ocupacoes = new ArrayList<>();
        recursos = new ArrayList<>();
        hangares = new ArrayList<>();
    }

    public void loginAPI(final String username, final String password, final Context context){
        if (!JsonParser.isConnectionInternet(context)){
            Toast.makeText(context, "Sem ligação à internet", Toast.LENGTH_LONG).show();
        }else
        {
            StringRequest req = new StringRequest(Request.Method.POST, mUrlAPILogin, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    String token = JsonParser.parserJsonLogin(response);
                    if (loginListener != null)
                        loginListener.onValidateLogin(token, username, context);
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("LOGIN: Error" + error.getMessage());
                    Toast.makeText(context, "Erro a efetuar o login!", Toast.LENGTH_LONG).show();
                }
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    //params.put("username", username);
                    //params.put("password", password);
                    String creds = String.format("%s:%s",username,password);
                    String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                    params.put("Authorization", auth);

                    return params;
                }
            };
            volleyQueue.add(req);
        }
    }

    public void getAllVoosAPI(final Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_USER, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(MainActivity.TOKEN, null);

        if (!JsonParser.isConnectionInternet(context)){
            Toast.makeText(context, "Sem ligação à internet", Toast.LENGTH_LONG).show();
            if (voosListener!=null)
            {
                voosListener.onRefreshListaVoos(myBDHelper.getAllVooBD());
            }
        }else
        {
            JsonArrayRequest req=new JsonArrayRequest(Request.Method.GET, mUrlAPIVoos + token, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    voos = JsonParser.parserJsonVoos(response);
                    adicionarVoosBD(voos);

                    if (voosListener!=null)
                    {
                        voosListener.onRefreshListaVoos(voos);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            volleyQueue.add(req);
        }
    }

    public void getAllTarefasAPI(int id_voo, final Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_USER, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(MainActivity.TOKEN, null);
        String mUrl = mUrlAPIBase+ "voo/"+ id_voo + "/alltarefa" + mUrlAPIAccessToken + token;
        if (!JsonParser.isConnectionInternet(context)){
            Toast.makeText(context, "Sem ligação à internet", Toast.LENGTH_LONG).show();
            if (tarefasListener !=null)
            {
                tarefasListener.onRefreshListaTarefas(myBDHelper.getAllTarefaBD());
            }
        }else
        {
            JsonArrayRequest req=new JsonArrayRequest(Request.Method.GET, mUrl, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    tarefas = JsonParser.parserJsonTarefas(response);
//                    System.out.println(tarefas);
                    adicionarTarefasBD(tarefas);

                    if (tarefasListener!=null)
                    {
                        tarefasListener.onRefreshListaTarefas(tarefas);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            volleyQueue.add(req);
        }
    }

    public void getTarefaSingleAPI(int id_tarefa, final Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_USER, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(MainActivity.TOKEN, null);
        String mUrl = mUrlAPIBase+ "tarefa/"+ id_tarefa+"/tarefainformation" + mUrlAPIAccessToken + token;
        if (!JsonParser.isConnectionInternet(context)){
            Toast.makeText(context, "Sem ligação á internet", Toast.LENGTH_LONG).show();
            if (tarefaSingleListener !=null)
            {
                tarefaSingleListener.onRefreshTarefaSingle(myBDHelper.getAllTarefaSingleBD());
            }
        }else
        {
            JsonObjectRequest req=new JsonObjectRequest(Request.Method.GET, mUrl, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    tarefaSingle = JsonParser.parserJsonTarefaSingle(response);
                    adicionarTarefaSingleBD(tarefaSingle);

                    if (tarefaSingleListener!=null)
                    {
                        tarefaSingleListener.onRefreshTarefaSingle(tarefaSingle);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            volleyQueue.add(req);
        }
    }


    public void editarTarefaSingleAPI(final TarefaSingle tarefaSingle, final Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_USER, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(MainActivity.TOKEN, null);
        String mUrl = mUrlAPIBase+ "tarefa/"+ tarefaSingle.getId() + mUrlAPIAccessToken + token;
        if (!JsonParser.isConnectionInternet(context)) {
            Toast.makeText(context, "Sem ligação á internet", Toast.LENGTH_LONG).show();
        } else {
            StringRequest req = new StringRequest(Request.Method.PUT, mUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    editarTarefaSingleDB(tarefaSingle);
                    if (tarefaSingleListener != null) {
                        tarefaSingleListener.onRefreshTarefaSingle(tarefaSingle);
                        Toast.makeText(context, "Tarefa editada", Toast.LENGTH_LONG).show();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("estado", tarefaSingle.getEstado());
                    return params;
                }
            };
            volleyQueue.add(req);
        }
    }

    public void removerTarefaSingleAPI(final TarefaSingle tarefaSingle, final Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_USER, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(MainActivity.TOKEN, null);
        String mUrl = mUrlAPIBase+ "tarefa/"+ tarefaSingle.getId() + mUrlAPIAccessToken + token;
        if (!JsonParser.isConnectionInternet(context)){
            Toast.makeText(context, "Sem ligação á internet", Toast.LENGTH_LONG).show();
        }else
        {
            StringRequest req = new StringRequest(Request.Method.DELETE, mUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    myBDHelper.removerAllTarefaSingle();
                    if (tarefaSingleListener!=null)
                    {
                        tarefaSingleListener.onRefreshTarefaSingle(tarefaSingle);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            volleyQueue.add(req);
        }
    }



    public void getAviaoAPI(int id_voo, final Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_USER, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(MainActivity.TOKEN, null);
        String mUrl = mUrlAPIBase+ "voo/"+ id_voo + "/aviao" + mUrlAPIAccessToken + token;
        if (!JsonParser.isConnectionInternet(context)){
            Toast.makeText(context, "Sem ligação á internet", Toast.LENGTH_LONG).show();
            if (aviaoListener !=null)
            {
                aviaoListener.onRefreshAviao(myBDHelper.getAllAviaoBD());
            }
        }else
        {
            JsonObjectRequest req=new JsonObjectRequest(Request.Method.GET, mUrl, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    aviao = JsonParser.parserJsonAviao(response);
                    //System.out.println(tarefas);
                    adicionarAviaoBD(aviao);

                    if (aviaoListener!=null)
                    {
                        aviaoListener.onRefreshAviao(aviao);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            volleyQueue.add(req);
        }
    }



    public void getPerfilAPI(final Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_USER, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(MainActivity.TOKEN, null);
        String mUrl = mUrlAPIBase+ "funcionario/"+ sharedPreferences.getString(MainActivity.USERNAME,null) + "/perfil" + mUrlAPIAccessToken + token;
        if (!JsonParser.isConnectionInternet(context)){
            Toast.makeText(context, "Sem ligação à internet", Toast.LENGTH_LONG).show();
            if (perfilListener !=null)
            {
                perfilListener.onRefreshPerfil(myBDHelper.getPerfilBD());

            }
        }else
        {

            JsonObjectRequest req =new JsonObjectRequest(Request.Method.GET, mUrl, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

//                    System.out.println(response);
                    perfil = JsonParser.parserJsonPerfil(response);
                    perfil.setId(1);
                    adicionarPerfilDB(perfil);

                    if (perfilListener!=null)
                    {
                        perfilListener.onRefreshPerfil(perfil);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            volleyQueue.add(req);
        }
    }


    public void getAllRecursoAPI(final Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_USER, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(MainActivity.TOKEN, null);
        String mUrl = mUrlAPIBase+ "recurso/all" + mUrlAPIAccessToken + token;
        if (!JsonParser.isConnectionInternet(context)){
            Toast.makeText(context, "Sem ligação á internet", Toast.LENGTH_LONG).show();
            if (recursosListener !=null)
            {
                recursosListener.onRefreshListaRecurso(myBDHelper.getAllRecursoBD());
            }
        }else
        {
            JsonArrayRequest req=new JsonArrayRequest(Request.Method.GET, mUrl, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    recursos = JsonParser.parserJsonRecursos(response);
                    //System.out.println(tarefas);
                    adicionarRecursosBD(recursos);

                    if (recursosListener!=null)
                    {
                        recursosListener.onRefreshListaRecurso(recursos);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            volleyQueue.add(req);
        }
    }

    public void getAllHangarAPI(final Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_USER, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(MainActivity.TOKEN, null);
        String mUrl = mUrlAPIBase+ "hangar/" + mUrlAPIAccessToken + token;
        if (!JsonParser.isConnectionInternet(context)){
            Toast.makeText(context, "Sem ligação á internet", Toast.LENGTH_LONG).show();
            if (hangarListener !=null)
            {
                hangarListener.onRefreshListaHangar(myBDHelper.getAllHangarBD());
            }
        }else
        {
            JsonArrayRequest req=new JsonArrayRequest(Request.Method.GET, mUrl, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    hangares = JsonParser.parserJsonHangares(response);
                    //System.out.println(hangares);
                    adicionarHangaresBD(hangares);

                    if (hangarListener!=null)
                    {
                        hangarListener.onRefreshListaHangar(hangares);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            volleyQueue.add(req);
        }
    }

    public void adicionarTarefaAPI(final Tarefa tarefa ,final Context context){
        if (!JsonParser.isConnectionInternet(context)){
            Toast.makeText(context, "Sem ligação á internet", Toast.LENGTH_LONG).show();
        }else
        {
            try {
                JSONObject params = new JSONObject();
                params.put("token", token);
                params.put("id_voo", tarefa.getId_voo()+"");// Transformar em string
                params.put("id_hangar", tarefa.getId_hangar()+"");
                params.put("id_recurso", tarefa.getId_recurso()+"");
                params.put("designacao", tarefa.getDesignacao());
                params.put("estado", tarefa.getEstado());
                params.put("id_funcionario_registo",4);
                params.put("quantidade", tarefa.getQuantidade());
//                System.out.println(params);
                // Building a request
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        // Using a variable for the domain is great for testing
                        mUrlAPIBase+ "tarefa"+ mUrlAPIAccessToken + token,
                        params,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Handle the response
                                //adicionarTarefaBD(VooJsonParser.parserJsonTarefa(response.toString()));
                            }
                        },

                        new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Handle the error
                                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                volleyQueue.add(request);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    /*public void removerTarefaAPI(final Tarefa tarefa, final Context context){
        if (!VooJsonParser.isConnectionInternet(context)){
            Toast.makeText(context, "Sem ligação á internet", Toast.LENGTH_LONG).show();
        }else
        {
            StringRequest req = new StringRequest(Request.Method.DELETE, mUrlAPIBase+ "tarefa/"+tarefa.getId()+ mUrlAPIAccessToken + token, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    removerTarefaBD(tarefa.getId());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            volleyQueue.add(req);
        }
    }*/

    //buscar tudo a base dados voo
    public ArrayList<Voo> getVoosBD() { // return da copia dos Voos
        voos= myBDHelper.getAllVooBD();
        return new ArrayList(voos);
    }

    public ArrayList<Hangar> getHangaresBD() { // return da copia dos Hangares
        hangares= myBDHelper.getAllHangarBD();
        return new ArrayList(hangares);
    }
    public ArrayList<Recurso> getRecursosBD() { // return da copia dos Hangares
        recursos= myBDHelper.getAllRecursoBD();
        return new ArrayList(recursos);
    }

    //buscar tudo a base dados perfil
    public Perfil getPerfilBD() { // return da copia dos Voos
        perfil= myBDHelper.getPerfilBD();

        return perfil;
    }

    public Voo getVoo(int idVoo){
        for (Voo V : voos){
            if(V.getId() == idVoo)
                return V;
        }
        return null;
    }
    //buscar tudo a base dados Tarefa
    public ArrayList<Tarefa> getTarefasBD() { // return da copia dos Tarefas
        tarefas= myBDHelper.getAllTarefaBD();
        return new ArrayList(tarefas);
    }

    public Tarefa getTarefa(int idVoo){
        for (Tarefa T : tarefas){
            if(T.getId_voo() == idVoo)
                return T;
        }
        return null;
    }


    public Aviao getAviao(){
        aviao= myBDHelper.getAllAviaoBD();
        return aviao;
    }

    //Adicionar base dados voo
    public void adicionarVoosBD(ArrayList<Voo> voos) {
        myBDHelper.removerAllVoo();
        for(Voo V:voos)
        {
            adicionarVooBD(V);
        }
    }
    public void adicionarVooBD(Voo v) {
        myBDHelper.adicionarVooBD(v);
    }


    //Adicionar base dados Tarefa
    public void adicionarPerfilDB(Perfil p) {
        myBDHelper.removerPerfilBD();
        myBDHelper.adicionarPerfilBD(p);
    }

    public void adicionarHangaresBD(ArrayList<Hangar> hangares) {
        myBDHelper.removerAllHangar();
        for(Hangar H:hangares)
        {
            adicionarHangarBD(H);
        }
    }
    public void adicionarHangarBD(Hangar H) {
        myBDHelper.adicionarHangarBD(H);}

    public void adicionarRecursosBD(ArrayList<Recurso> recursos) {
        myBDHelper.removerAllRecurso();
        for(Recurso R:recursos)
        {
            adicionarRecursoBD(R);
        }
    }
    public void adicionarRecursoBD(Recurso R) {
        myBDHelper.adicionarRecursoBD(R);}

    //Adicionar base dados Tarefa
    public void adicionarTarefasBD(ArrayList<Tarefa> tarefas) {
        myBDHelper.removerAllTarefa();
        for(Tarefa T:tarefas)
        {
            adicionarTarefaBD(T);
        }
    }
    public void adicionarTarefaBD(Tarefa T) {
        myBDHelper.adicionarTarefaBD(T);}

    //Adicionar base dados Aviao
    public void adicionarAviaosBD(ArrayList<Aviao> aviaos)
    {
        myBDHelper.removerAllAviao();
        for(Aviao A:aviaos)
        {
            adicionarAviaoBD(A);
        }
    }

    public void adicionarAviaoBD(Aviao A) {
        myBDHelper.adicionarAviaoBD(A);}

    public void adicionarTarefaSingleBD(TarefaSingle T) {
        myBDHelper.adicionarTarefaSingleBD(T);}

    //remover Tarefa
    public void removerTarefaBD(int id)
    {
        Tarefa auxTarefa = getTarefa(id);
        if (auxTarefa!=null)
            myBDHelper.removerTarefaBD(id);
    }

    public void editarTarefaSingleDB(TarefaSingle t)
    {
            myBDHelper.editarTarefaSingleBD(t);

    }




}
