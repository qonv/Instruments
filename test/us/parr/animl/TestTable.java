
package us.parr.animl;

import org.junit.Test;
import us.parr.animl.data.DataTable;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static us.parr.animl.data.DataTable.VariableType.CATEGORICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.CATEGORICAL_STRING;
import static us.parr.animl.data.DataTable.VariableType.NUMERICAL_FLOAT;
import static us.parr.animl.data.DataTable.VariableType.NUMERICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.TARGET_CATEGORICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.TARGET_CATEGORICAL_STRING;
import static us.parr.animl.data.DataTable.VariableType.UNUSED_INT;
import static us.parr.lib.ParrtCollections.join;

public class TestTable extends BaseTest {
	public static final String[] colNames3 = {"a", "b", "y"};