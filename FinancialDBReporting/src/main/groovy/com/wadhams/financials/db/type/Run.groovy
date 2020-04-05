package com.wadhams.financials.db.type

enum Run {
	TimestampReport('TSR'),
	OverWriteReport('OWR'),
	Unknown('Unknown');
	
	private static EnumSet<Run> allEnums = EnumSet.allOf(Run.class)
	
	private final String name

	Run(String name) {
		this.name = name
	}
	
	public static Run findByName(String text) {
		if (text) {
			text = text.toUpperCase()
			for (Run e : allEnums) {
				if (e.name.equals(text)) {
					return e
				}
			}
		}
		else {
			return Run.Unknown
		}
		return Run.Unknown
	}

}
