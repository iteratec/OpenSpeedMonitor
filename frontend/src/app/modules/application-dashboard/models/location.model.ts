export interface BrowserDto {
  id: number
  name: string
}
export interface LocationDto {
  id: Number
  name: string
  parent: BrowserDto
}
