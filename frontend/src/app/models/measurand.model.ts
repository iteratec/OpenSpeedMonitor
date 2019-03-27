
export interface SelectableMeasurand {
  name: string
  id: string
}

export  interface MeasurandGroup {
  name: string,
  values: SelectableMeasurand[]
}
