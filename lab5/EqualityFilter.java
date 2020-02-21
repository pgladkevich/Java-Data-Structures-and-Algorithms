/**
 * TableFilter to filter for entries equal to a given string.
 *
 * @author Matthew Owen
 */
public class EqualityFilter extends TableFilter {

    public EqualityFilter(Table input, String colName, String match) {
        super(input);
        this._table = input;
        this._colName = colName;
        this._match = match;
    }

    @Override
    protected boolean keep() {
        Table.TableRow curRow = this.candidateNext();
        String s1 = curRow.getValue(_table.colNameToIndex(_colName));
        String s2 = _match;
        if (s1.equals(s2) && s2 != null) {
            return true;
        }
        return false;
    }

    private Table _table;
    private String _colName;
    private String _match;
    private Table.TableRow _curRow;
}
