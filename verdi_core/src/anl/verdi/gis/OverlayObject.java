package anl.verdi.gis;

import anl.verdi.data.Variable;
import anl.verdi.plot.gui.ObsAnnotation;
import anl.verdi.plot.gui.ObsAnnotation.Symbol;

public class OverlayObject {
	private int strokeSize;
	private int shapeSize;
	private ObsAnnotation.Symbol symbol;
	private Variable var;
	
	public OverlayObject(Variable variable, Symbol sym, int stroke, int shape) {
		var = variable;
		symbol = sym;
		strokeSize = stroke;
		shapeSize = shape;
	}
	
	public Variable getVariable() {
		return var;
	}
	
	public void setVariable(Variable variable) {
		var = variable;
	}
	
	public Symbol getSymbol() {
		return symbol;
	}
	
	public void setSymbol(Symbol symbol) {
		this.symbol = symbol;
	}
	
	public int getStrokeSize() {
		return strokeSize;
	}
	
	public void setStrokeSize(int size) {
		strokeSize = size;
	}
	
	public int getShapeSize() {
		return shapeSize;
	}
	
	public void setShapeSize(int size) {
		shapeSize = size;
	}
	
	@Override
	public boolean equals (Object other) {
		if (var == null)
			return false;
		
		if (other == null || ((OverlayObject)other).getVariable() == null)
			return false;
		
		if (var.getDataset() == null)
			return true;

		OverlayObject o = (OverlayObject) other;
		return var.getName().equalsIgnoreCase(o.getVariable().getName()) &&
			var.getDataset().getURL().getFile().equals(o.getVariable().getDataset().getURL().getFile());
			
	}
	
	public String toString() {
		if (var == null)
			return "Variable: null";
		
		if (var.getName() != null && var.getDataset() == null)
			return var.getName() + ": null";
		
		return var.getName() + var.getDataset().getAlias() 
			+ " (" + symbol + ", stroke--" + strokeSize + ", size--" + shapeSize + ")";
	}
}
