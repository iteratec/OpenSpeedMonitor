
export interface IPageId {
  name: string;
  id: number;
}
export interface IPage extends IPageId{
  undefinedPage: boolean
  jobGroupId: number
}

