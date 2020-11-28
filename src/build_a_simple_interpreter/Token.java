package build_a_simple_interpreter;

// https://www.cnblogs.com/xiehy/archive/2010/02/04/1663825.html 运算符优先级
// 算数运算符>关系运算符>逻辑运算符>赋值运算符
/** 词素类型 */
public enum Token {
	Error, None, //错误类型, 无效类型
	NumberLiteral, StringLiteral, Identifier, Punctuation, Comment, End, //
	Import, Class, Interface, Enum, Extends, Implements, //
	If, Else, While, Do, For, Continue, Break, Return, Sizeof, //
	//并列运算符
	Comma, // ,   
	//赋值运算符(从右到左)
	Assign, AssignAdd, AssignSub, AssignMul, AssignDiv, AssignMod, Question, // = += -= *= /= %= ? 
	//二元运算符(从左到右)
	//|| &&    |    ^     &     ==  !=  <   >   <=  >=  <<   >>   +    -    *    /    %
	Lor, Land, Bor, Bxor, Band, Eq, Ne, Lt, Gt, Le, Ge, Shl, Shr, Add, Sub, Mul, Div, Mod,
	//一元运算符(从右到左)
	Inc, Dec, Lnot, Binv, // ++ -- ! ~ -
	//成员运算符
	BrackL, ParenL, Dot, // [ ( .   
	//其他
	ParenR, BrackR, BraceL, BraceR, Colon, Sharp, SemiColon, Quote, QuoteSingle, Slope
	//  )   ]       {       }       :      #      ;          "      '            \     
}
