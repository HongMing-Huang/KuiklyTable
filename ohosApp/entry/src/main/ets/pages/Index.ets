/*
 * Tencent is pleased to support the open source community by making KuiklyUI
 * available.
 * Copyright (C) 2025 Tencent. All rights reserved.
 * Licensed under the License of KuiklyUI;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://github.com/Tencent-TDS/KuiklyUI/blob/main/LICENSE
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { KRNativeRenderController, KRRecord, Kuikly } from '@kuikly-open/render';
import { hilog } from '@kit.PerformanceAnalysisKit';

@Entry
@Component
struct Index {
  private kuiklyController: KRNativeRenderController | null = null;
  private pageName: string = 'TableDemoPage';
  private pageData: KRRecord = {};
  @State showKuikly: boolean = true;

  build() {
    Stack({ alignContent: Alignment.TopStart }) {
      if (this.showKuikly) {
        Kuikly({
          pageName: this.pageName,
          pageData: this.pageData,
          contextCode: '',
          useOhSharedPreferences: true,
          onControllerReadyCallback: (controller) => {
            this.kuiklyController = controller;
            controller.registerExceptionCallback((executeMode, stack) => {
              const stackInfo: KRRecord = JSON.parse(stack);
              stackInfo['stack'].toString().split('\n').forEach((it) => {
                hilog.error(0x0000, 'demo', '%{public}s', it);
              });
            });
          },
        });
      }
    }
    .width('100%')
    .height('100%')
    .expandSafeArea([SafeAreaType.KEYBOARD]);
  }
}
