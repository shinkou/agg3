class MySuite extends munit.FunSuite {
  test("Data type detection") {
    assertEquals(AggOp.intDblStr("0.9"), 2)
    assertEquals(AggOp.intDblStr("0"), 1)
    assertEquals(AggOp.intDblStr("0123"), 0)
    assertEquals(AggOp.intDblStr("1.0"), 2)
  }
}
