export class PageComparisonSelection {
  firstJobGroupId: string = '-1';
  firstPageId: string = '-1';
  secondJobGroupId: string = '-1';
  secondPageId: string = '-1';

  isValid():boolean {
    return this.firstJobGroupId !== '-1' && this.secondJobGroupId !== '-1' && this.firstPageId !== '-1' && this.secondPageId !== '-1';
  }
}
