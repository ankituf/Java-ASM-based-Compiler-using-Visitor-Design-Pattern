package cop5556sp17;
import java.util.*;
import cop5556sp17.AST.Dec;

class Content
{
	int scope_num;
	Dec dec;
	public Content(int temp_scope,Dec temp_dec)
	{
		this.scope_num=temp_scope;
		this.dec=temp_dec;
	}
	public Dec getDec()
	{
		return dec;
	}
	public int getSnum()
	{
		return scope_num;
	}
}
public class SymbolTable {
	int current,next;
	Stack<Integer> st =new Stack<Integer>();
	HashMap<String,ArrayList<Content>> hm=new HashMap<String,ArrayList<Content>>();

	public void enterScope()
	{
		current=++next;
		st.push(current);
	}

	public void leaveScope()
	{
		st.pop();
		current=st.peek();
	}

	public boolean insert(String ident, Dec dec)
	{
		ArrayList<Content> al=new ArrayList<Content>();
		Content st=new Content(current,dec);
		if(hm.containsKey(ident))
		{
			al=hm.get(ident);
			for(int i=0;i<al.size();i++)
			{
				Content c=al.get(i);
				if(c.scope_num==current)
					return false;
			}
		}
		al.add(st);
		hm.put(ident, al);
		return true;
	}
	public Dec lookup(String ident){
		ArrayList<Content> steList=new ArrayList<Content>();
		if(!hm.containsKey(ident))
		return null;
		Dec dec=null;
		steList=hm.get(ident);
		for(int i=(steList.size())-1;i>=0;i--)
		{
			int tmp=steList.get(i).getSnum();
			if(st.contains(tmp))
			{
				dec=steList.get(i).getDec();
				break;
			}
		}
		return dec;
		}
	public SymbolTable()
	{
		this.current=0;
		this.next=0;
		st.push(0);
	}

	@Override
	public String toString() 
	{
		return this.toString();
	}




}