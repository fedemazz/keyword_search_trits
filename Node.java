import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class Node {

    private int r; //dimensione ipercubo
    private int n; //numero del nodo 
    private String id; //stringa id del nodo, composta dal suo codice binario
    private TritSet tritset;
    private ArrayList<Node> neighbors; //in un implementazione reale sarebbero gli indirizzi?
    private Map<String, String> nodeList; //la lista degli id di tutti gli altri nodi 
    private Map<Set<String>, ArrayList<String>> references; //coppia chiave valore, dove il valore in un implementazione reale sarebbe l'indirizzo di una transazione/canale iota
    private Map<String, String> objects;

    public Node(){
    }

    public Node (int n, int r){
        this.r = r;//r è la dimensione dell'ipercubo
        this.n = n; //numero del nodo che verrà trasformato in binario
        this.tritset = new TritSet(n,r);
        this.id = this.tritset.getValue();
        //this.bitset = createBitset(this.id);
        this.neighbors = new ArrayList<Node>();
        this.nodeList = createNodeList();
        this.references = new HashMap<Set<String>, ArrayList<String>>();
        this.objects = new HashMap<String, String>();
    }

    public int getR(){
        return this.r;
    }

    public int getN(){
        return this.n;
    }

    public String getId(){
        return this.id;
    }

    public TritSet getTritSet(){
        return this.tritset;
    }
    
    private Map<String, String> createNodeList(){
        Map<String, String> list = new  HashMap<String, String>();
        for (int i = 0; i <Math.pow(3, getR()); i++){
            String currentID = new TritSet(i,getR()).getValue();
            list.put(currentID, "indirizzo: " + currentID);
        }

        return list;
    } 

    //setto i vicini del nodo
    public void setNeighbors(Map <String, Node> nodes) {
        //scorro l'hashMap contenente tutti i nodi dell'ipercubo istanziati 
       //e li confronto agli stessi, per trovare i "neighbors" dei vari nodi
       //cioè quelli che differiscono di un bit rispetto al nodo trattato
        for (Map.Entry<String, Node> entry : nodes.entrySet()) {
            if (this.tritset.differOneTrit(entry.toString())){
            neighbors.add(entry.getValue());
            }
        }
       }

    //restituisco i vicini del nodo
    public ArrayList<Node> getNeighbors(){
        return this.neighbors;
    }

    public Set<String> getNodeList(){
        return nodeList.keySet();
    }


    //restituiscono i vicini del nodo i cui bitset includono il nodo stesso
    //tra tutti i vicini prendo solo quelli che soddisfano la condizione isIncluded
    public ArrayList<Node> getNeighborsIncluded(){
        ArrayList<Node> neighborsIncluded = new ArrayList<Node>();
        for (Node neighbor : this.getNeighbors()) {
            if (this.getTritSet().isIncludedIn(neighbor.getTritSet())) {
                neighborsIncluded.add(neighbor);
            }
        }
        return neighborsIncluded;
    }

 

    public ArrayList<String> getReference(Set<String> key) {
        if (this.references.containsKey(key)){
            return this.references.get(key);
        } else return null;
    }

    public String getObject(String key) {
        if (this.objects.containsKey(key)){
            return this.objects.get(key);
        } else return null;
    }

    public boolean hasNeighbor(String id){
        for (Node neighbor : this.getNeighbors()) {
            if (neighbor.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }


    public Node findTargetNode(TritSet tritSet){
        System.out.println("Cerco nodo...");
        if (this.getTritSet().equals(tritSet)){
            return this;
        }
        else {
            return this.nearestNode(tritSet).findTargetNode(tritSet);
        }
    }

    public TritSet generateTritSet(Set<String> keySet){
        
        String kSetString = "";
        for (int i = 0; i < getR(); i++){
            kSetString = kSetString +"0";
        }
        StringBuilder temp = new StringBuilder(kSetString);
        for (String entry : keySet){
            int kBit = hashFunction(entry, r);
            //int kBitValue = hashFunction3(entry);
            // setto il k-esimo bit del bitset di ricerca kSet ad 1
            

            //in base al valore dell'hashFunction3 (valore da 1 a 2) setto il bit di interesse 
            /*if(kBitValue ==1){
            temp.setCharAt(getR()-1  - kBit, '1'); }
            else if (kBitValue == 2)            
            temp.setCharAt(getR()-1  - kBit, '2');*/

            //non uso hashFunction3 ma quante volte si ripetono keyword che danno lo stesso hash
            if (temp.charAt(getR()-1  - kBit) == '0'){
                temp.setCharAt(getR()-1  - kBit, '1');
            } else {
                temp.setCharAt(getR()-1  - kBit, '2');
            }
        }
        return new TritSet(temp.toString(),this.r);
    }

    
    private static int hashFunction(String key, int r){
        return key.hashCode()%r;
    }

    private static int hashFunction3(String key){
        return key.hashCode()%2+1;
    }

    public Node nearestNode(TritSet targetSet){
        for(Node entry : this.getNeighbors()){
            if (entry.getTritSet().xor(targetSet).cardinality() < this.getTritSet().xor(targetSet).cardinality()){
                return entry;
            }
        }
        return null;
    }

    


    //controllo se bitset2 può essere inserito come figlio di bitset 1 nell'SBT
    //il criterio è che il bit di differena tra bs1 e bs2 sia a destra dell'ultimo bit di bs1
    //01100 e 01110. bs2 è children
    //01100 e 11000. bs2 non è children
    /*public boolean isChildren(BitSet bitSet1, BitSet bitSet2){
        BitSet childrenBitSet = new BitSet();
        BitSet bs1Temp = new BitSet();
        BitSet bs2Temp = new BitSet();
        childrenBitSet.or(bitSet1); 
        bs1Temp.or(bitSet1);
        bs2Temp.or(bitSet2);
        bs1Temp.xor(bs2Temp);
        if(bs1Temp.nextSetBit(0) <= childrenBitSet.nextSetBit(0)){
            return true;
        }
        return false;
    }


    //creo l'sbt relativo ad un set di keyword cercato
    //il set di keyword cercato è quello su cui è chiamato questo metodo
    public NodeSBT generateSBT (boolean init){

        NodeSBT root = new NodeSBT(this.getId(), this.getOne());
        for(Node entry : this.getNeighborsIncluded()) {                      
            //se il primo bit (posizione 0) è settato a 1 sono arrivato ad una foglia
            if (entry.getOne().nextSetBit(0) == 0) {
                    root.addChild(new NodeSBT(entry.getId(), entry.getOne()));
            }
                else {
                    //la prima volta aggiungo tutti i vicini con un bit di differenza all'albero (sono figli di root)
                    if (init) {
                        root.addChild(entry.generateSBT(false));
                    } 
                    else {  
                        //caso ricorsivo per il livello superiore al secondo
                        //forse ci va anche questo controllo  this.getOne().previousSetBit(getR()) == entry.getOne().previousSetBit(getR())  
                        if (isChildren(this.getOne(), entry.getOne())){
                        root.addChild(entry.generateSBT(false));
                        }
                    }
            }
        }
        return root;
    } */

    public static String getMd5(String input) { 
        try { 
  
            // Static getInstance method is called with hashing MD5 
            MessageDigest md = MessageDigest.getInstance("MD5"); 
  
            // digest() method is called to calculate message digest 
            //  of an input digest() return array of byte 
            byte[] messageDigest = md.digest(input.getBytes()); 
  
            // Convert byte array into signum representation 
            BigInteger no = new BigInteger(1, messageDigest); 
  
            // Convert message digest into hex value 
            String hashtext = no.toString(16); 
            while (hashtext.length() < 32) { 
                hashtext = "0" + hashtext; 
            } 
            return hashtext; 
        }  
  
        // For specifying wrong message digest algorithms 
        catch (NoSuchAlgorithmException e) { 
            throw new RuntimeException(e); 
        } 
    } 

    public void addObject(Hypercube hypercube, Set<String> oKey, String oValue){
        String idObject = getMd5(oValue);
        //inserisco l'oggetto nella lista objects del nodo che ha avviato la richiesta di inserimento
        this.objects.put(idObject, oValue);

        //inserisco l'associazione reference <σ, u> nel nodo L(σ) (in questo caso come detto lo inserisco in una hash table comune che esegue il mapping DHT)
        Insert(hypercube, idObject, this.getId());

        //eseguire il controllo se la copia esiste

        //inserisco nel nodo che si occupa del set di keyword la coppia <Kσ, σ>
        Insert(findTargetNode(generateTritSet(oKey)), oKey, idObject);
    }

    private void Insert(Hypercube hypercube, String idObject, String idNode){
        hypercube.addMapping(idObject, idNode);
    }

    //metodo per aggiungere alla lista di reference la coppia <Kσ, σ>
    //va aggiunta nel nodo che si occupa di Kσ, chiamato responsible
    private void Insert(Node responsible, Set<String> oKey, String idObject){
        responsible.addReference(oKey, idObject);
    }

    private void addReference(Set<String> oKey, String idObject) {
        //se nelle reference è gia presente la keyword, aggiungo l'oggetto alla lista di oggetti per quella keyword
        if (this.references.containsKey(oKey)) {
            this.references.get(oKey).add(idObject);
        } else {
            //altrimenti creo una nuova entry nell'index contente, per ora, solamente la reference dell'ogetto aggiunto
            ArrayList<String> object = new ArrayList<String>();
            object.add(idObject);
            this.references.put(oKey, object);
        }
    }

    public ArrayList<String> getObjects(Hypercube hypercube, Set<String> keySet, int c){
        
        ArrayList<String> result = new ArrayList<String>();
        //hash table references <Kσ, σ>
        //do il set di keyword in input al nodo che lo gestisce (K)
        //ottengo una lista di id. {σ1....σn}
        //Ogni id si riferisce ad un oggetto differente. Tutti gli oggetti hanno in comune il set di keyword 
        ArrayList<String> reference = new ArrayList<String>(this.findTargetNode(generateTritSet(keySet)).getReference(keySet));

        //controllo sul conteggio dei risultati 
        //se inferiore ai risultati attesi esplorare SBT
        //NodeSBT sbtRoot = generateSBT(true);

        //se ho risultati
        if (reference != null){
            //per ogni oggetto
            for (String idObject : reference){
                //cerco il nodo che mantiene l'oggetto σ (ci arrivo attraverso l'hash table <σ, u>)          
                //e recupero da lui l'oggetto vero e proprio
                result.add(this.findTargetNode(new TritSet(hypercube.getMapping(idObject), this.r)).getObject(idObject));
            }
        }
        return result;
    }
}

