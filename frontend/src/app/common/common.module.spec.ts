import {OsmCommonModule} from './common.module';

describe('CommonModule', () => {
  let commonModule: OsmCommonModule;

  beforeEach(() => {
    commonModule = new OsmCommonModule();
  });

  it('should create an instance', () => {
    expect(commonModule).toBeTruthy();
  });
});
