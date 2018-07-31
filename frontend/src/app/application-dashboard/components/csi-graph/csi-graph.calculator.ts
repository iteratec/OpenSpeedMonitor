import {scaleLinear, ScaleLinear, scaleTime, ScaleTime} from "d3-scale";
import {area, Area, curveLinear, line, Line} from "d3-shape";
import {CsiDTO} from "../../models/csi.model";

export class CsiGraphCalculator {
  lineGenerator: Line<CsiDTO>;
  areaGenerator: Area<CsiDTO>;
  yScale: ScaleLinear<number, number>;
  xScale: ScaleTime<number, number>;

  constructor(width: number, height: number) {
    if (width && height) {
      this.xScale = CsiGraphCalculator.getXScale(width);
      this.yScale = CsiGraphCalculator.getYScale(height);
      this.lineGenerator = this.getLineGenerator();
      this.areaGenerator = this.getAreaGenerator();
    }
  }

  isValid(): boolean {
    return !!this.xScale && !!this.yScale && !!this.areaGenerator && !!this.lineGenerator
  }

  private static getXScale(width: number): ScaleTime<number, number> {
    const offset = (24 * 60 * 60 * 1000) * 7 * 4; //4 Weeks;
    let endDate: Date = CsiGraphCalculator.dayStart(new Date(Date.now()));
    const startDate: Date = new Date(endDate.getTime() - offset);
    return scaleTime().domain([startDate, endDate]).range([0, width]);
  }

  private static getYScale(height: number): ScaleLinear<number, number> {
    return scaleLinear().domain([0, 100]).range([height, 0]);
  }

  private static dayStart(input: Date): Date {
    let date: Date = new Date(input.getTime());
    date.setUTCHours(0, 0, 0, 0);
    return date;
  }

  private getLineGenerator(): Line<CsiDTO> {
    return line<CsiDTO>()
      .curve(curveLinear)
      .x((csiDTO: CsiDTO) => this.calculateX(csiDTO))
      .y((csiDTO: CsiDTO) => this.calculateY(csiDTO))
  }

  private getAreaGenerator(): Area<CsiDTO> {
    return area<CsiDTO>()
      .x((csiDTO: CsiDTO) => this.calculateX(csiDTO))
      .y1((csiDTO: CsiDTO) => this.calculateY(csiDTO))
      .y0(this.yScale(0))
  }

  private calculateX(csiDTO: CsiDTO): number {
    return this.xScale(CsiGraphCalculator.dayStart(new Date(csiDTO.date)));
  }

  private calculateY(csiDTO: CsiDTO): number {
    return this.yScale(csiDTO.csiDocComplete);
  }
}
