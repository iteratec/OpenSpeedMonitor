export interface PageIdDto {
  name: string;
  id: number;
}

export interface PageDto extends PageIdDto {
  undefinedPage: boolean
  jobGroupId: number
}
