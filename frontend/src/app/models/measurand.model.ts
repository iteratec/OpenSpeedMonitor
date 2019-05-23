
export interface SelectableMeasurand {
  name: string
  id: string
  isUserTiming?: boolean
}

export  interface MeasurandGroup {
  name: string,
  values: SelectableMeasurand[]
}
