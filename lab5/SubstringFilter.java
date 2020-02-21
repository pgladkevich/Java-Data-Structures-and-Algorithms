/**
 * TableFilter to filter for containing substrings.
 *
 * @author Matthew Owen
 */
public class SubstringFilter extends TableFilter {

    public SubstringFilter(Table input, String colName, String subStr) {
        super(input);
        this._table = input;
        this._colName = colName;
        this._subStr = subStr;
    }

    @Override
    protected boolean keep() {
        Table.TableRow curRow = this.candidateNext();
        String s1 = curRow.getValue(_table.colNameToIndex(_colName));
        String s2 = _subStr;
        if (s1.contains(_subStr)) {
            return true;
        }
        return false;
    }

    private Table _table;
    private String _colName;
    private String _subStr;
    private Table.TableRow _curRow;
}
