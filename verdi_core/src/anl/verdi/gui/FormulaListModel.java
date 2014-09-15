package anl.verdi.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;

/**
 * Backing model for the formulas gui.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class FormulaListModel extends AbstractListModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6783238921552068598L;
	private List<FormulaListElement> formulas = new ArrayList<FormulaListElement>();
	private ListSelectionModel selectionModel;

	public FormulaListModel()
	{
		System.out.println("in FormulaListModel default constructor");
	}
	

	public ListSelectionModel getSelectionModel() {
		System.out.println("in FormulaListModel getSelectionModel");
		return selectionModel;
	}

	public void setSelectionModel(ListSelectionModel selectionModel) {
		System.out.println("in FormulaListModel setSelectionModel");
		this.selectionModel = selectionModel;
	}

	public Object getElementAt(int index) {
		System.out.println("in formulaListModel getElementAt, returns Object");
		return formulas.get(index);
	}

	public String getFormulaAt(int index) {
		System.out.println("in formulaListModel getElementAt, returns String");
		return formulas.get(index).getFormula();
	}

	public int getSize() {
//		System.out.println("in formulaListModel getSize");		// NOTE called VERY frequently
		return formulas == null ? 0 : formulas.size();
	}

	private int indexOf(String formula) {
		System.out.println("in FormulaListModel indexOf");
		int index = 0;
		for (FormulaListElement dt : formulas) {

			if (dt.getFormula().trim().equals(formula.trim())) return index;
			index++;
		}
		return -1;
	}

	public int addFormula(String formula) {
		System.out.println("in FormulaListModel addFormula String");
		return addFormula(new FormulaListElement(formula));
	}

	public int addFormula(FormulaListElement element) {
		System.out.println("in FormulaListModel addFormula as FormulaListElement");
		int index = indexOf(element.getFormula());
		if (index == -1) {
			index = formulas.size();
			formulas.add(element);
			fireIntervalAdded(this, index, index);
		}
		return index;
	}

	public Iterable<FormulaListElement> elements() {
		System.out.println("in FormulaListModel elements");
		return formulas;
	}

	public void removeFormulaAt(int index) {
		System.out.println("in FormulaListModel removeFormulaAt");
		//FormulaListElement element = formulas.remove(index);
		formulas.remove(index);
		fireIntervalRemoved(this, index, index);
	}

	public void removeFormula(FormulaListElement element) {
		System.out.println("in FormulaListModel removeFormula");
		int index = formulas.indexOf(element);
		formulas.remove(index);
		fireIntervalRemoved(this, index, index);
	}

	/**
	 * Removes all the elements from this model.
	 */
	public void clear() {
		System.out.println("in FormulaListModel clear");
		int size = formulas.size();
		formulas.clear();
		fireIntervalRemoved(this, 0, size);
	}

	public void addAll(List<FormulaListElement> elements) {
		System.out.println("in formulaListModel addAll");
		if (elements.size() > 0) {
			int index = formulas.size();
			formulas.addAll(elements);
			fireIntervalAdded(this, index, formulas.size() - 1);
		}
	}

	public void setSelectedItem(int index) {
		System.out.println("in FormulaListModel setSelectedItem  index");
		if (selectionModel != null) selectionModel.setSelectionInterval(index, index);
	}

	public void setSelectedItem(FormulaListElement element) {
		System.out.println("in FormulaListModel setSelectedItem  element");
		if (element != null && selectionModel != null) {
			int i = indexOf(element.getFormula());
			if (i != -1) setSelectedItem(i);
		}
	}
}
