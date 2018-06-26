package demo;

import java.util.LinkedList;

import javax.swing.table.AbstractTableModel;

import aqpeq.utilities.StringPoolUtility;
import queryExpansion.CostAndNodesOfAnswersPair;

public class TableModel extends AbstractTableModel {

	LinkedList<CostAndNodesOfAnswersPair> suggestedKW;
	String[] columnNames = { "Add", "Suggestion", "Cost", "Div", "TFIDF", "Importance" };
	Object[][] data = new Object[0][6];

	public TableModel(LinkedList<CostAndNodesOfAnswersPair> suggestedKW) {
		this.suggestedKW = suggestedKW;
		this.data = new Object[suggestedKW.size()][6];
		int i = 0;
		for (CostAndNodesOfAnswersPair kw : suggestedKW) {

			try {
				data[i][0] = new Boolean(false);
				data[i][1] = StringPoolUtility.getStringOfId(kw.keywordIndex);
				data[i][2] = kw.cost;
				double FScore = kw.FScore * 100;
				FScore = Math.round(FScore);
				FScore = FScore / 100;
				double tfidf = kw.getTfIdf() * 100;
				tfidf = Math.round(tfidf);
				tfidf = tfidf / 100;
				double importance = kw.getImportance() * 100;
				importance = Math.round(importance);
				importance = importance / 100;
				data[i][3] = FScore;
				data[i][4] = tfidf;
				data[i][5] = importance;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			i++;
		}
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return columnNames.length;
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return data.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return data[rowIndex][columnIndex];
	}
	
    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col == 0) {
            return true;
        } else {
            return false;
        }
    }

}
