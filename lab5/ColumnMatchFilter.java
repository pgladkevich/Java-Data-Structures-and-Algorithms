import com.sun.codemodel.internal.JType;
import java.util.ArrayList;

import java.util.HashMap;

/**
 * TableFilter to filter for entries whose two columns match.
 *
 * @author Matthew Owen
 */
public class ColumnMatchFilter extends TableFilter {

    public ColumnMatchFilter(Table input, String colName1, String colName2) {
        super(input);
        this._table = input;
        this._colName1 = colName1;
        this._colName2 = colName2;
    }

    @Override
    protected boolean keep() {
        Table.TableRow curRow = this.candidateNext();
        String s1 = curRow.getValue(_table.colNameToIndex(_colName1));
        String s2 = curRow.getValue(_table.colNameToIndex(_colName2));
        if (s1.equals(s2)) {
            return true;
        }
        return false;
    }

    private Table _table;
    private String _colName1;
    private String _colName2;
    private Table.TableRow _curRow;
}
