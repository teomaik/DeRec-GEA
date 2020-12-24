package metrics;

public class Metrics {
	protected float ana;
	protected float moa;
	protected float nop;
	protected float cbo;
	protected float lcom;
	protected float npm;
	protected float wmc;
	protected float mfa;
	protected float dam;
	protected int dac; 
	protected int size2; 
	protected float nom; 
	protected int dcc; 
	protected int dsc; 
	protected int noh; 
	protected double camc;
	protected int size1;
	protected double wmc_cc;
	protected int mpc;
	protected double reusability; 
	protected double flexibility; 
	protected double understandability; 
	protected double functionality; 
	protected double extendibility; 
	protected double effectiveness; 

	public Metrics() {
		this.ana = 0.0F;
		this.moa = 0.0F;
		this.nop = 0.0F;
		this.cbo = 0.0F;
		this.npm = 0.0F;
		this.wmc = 0.0F;
		this.mfa = 0.0F;
		this.dam = 0.0F;
		this.dac = 0; 
		this.size2 = 0; 
		this.nom = 0.0F; 
		this.dcc = 0; 
		this.dsc = 0; 
		this.noh = 0; 
		this.camc = 0; 
		this.size1 = 0;
		this.wmc_cc = 0;
		this.mpc = 0;
		this.reusability = 0; 
		this.flexibility = 0; 
		this.understandability = 0; 
		this.functionality = 0; 
		this.extendibility = 0; 
		this.effectiveness = 0; 
	}
	public int getDac() {
		return this.dac;
	}

	public void setDac(int dac) {
		this.dac = dac;
	}

	public int getSize2(){
		return this.size2;
	}
	
	public void setSize2(int size2){
		this.size2 = size2;
	}
	
	public void setMaintainability(){
		this.nom = this.wmc;
	}

	public int getDcc() {
		return this.dcc;
	}
	
	public int getNoh(){
		return this.noh;
	}
	
	public void setCamc(double camc){
		this.camc = camc;
	}
	
	public void setSize1(int size){
		this.size1 = size;
	}
	
	public void setWmcCc(double wmc){
		this.wmc_cc = wmc;
	}
	
	public void setMpc(int mpc) {
		this.mpc = mpc;
	}

	public float getAna() {
		return this.ana;
	}

	public void setAna(float ana) {
		this.ana = ana;
	}

	public float getMoa() {
		return this.moa;
	}

	public void setMoa(float moa) {
		this.moa = moa;
	}

	public float getNop() {
		return this.nop;
	}

	public void setNop(float nop) {
		this.nop = nop;
	}

	public float getCbo() {
		return this.cbo;
	}

	public void setCbo(float cbo) {
		this.cbo = cbo;
	}

	public float getLcom() {
		return this.lcom;
	}

	public void setLcom(float lcom) {
		this.lcom = lcom;
	}

	public float getNpm() {
		return this.npm;
	}

	public void setNpm(float npm) {
		this.npm = npm;
	}

	public float getWmc() {
		return this.wmc;
	}

	public void setWmc(float wmc) {
		this.wmc = wmc;
	}

	public float getMfa() {
		return this.mfa;
	}

	public void setMfa(float mfa) {
		this.mfa = mfa;
	}

	public float getDam() {
		return this.dam;
	}

	public void setDam(float dam) {
		this.dam = dam;
	}
}
