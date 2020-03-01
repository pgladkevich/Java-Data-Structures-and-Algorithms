/**
 * TableFilter to filter for entries greater than a given string.
 *
 * @author Matthew Owen
 */
public class GreaterThanFilter extends TableFilter {

    public GreaterThanFilter(Table input, String colName, String ref) {
        super(input);
        this._table = input;
        this._colName = colName;
        this._ref = ref;
    }

    @Override
    protected boolean keep() {
        Table.TableRow curRow = this.candidateNext();
        String s1 = curRow.getValue(_table.colNameToIndex(_colName));
        String s2 = _ref;
        if (s1.compareTo(s2) > 0) {
            return true;
        }
        return false;
    }

    private Table _table;
    private String _colName;
    private String _ref;
    private Table.TableRow _curRow;
}
